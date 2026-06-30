package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.dto.GuestDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelReportDto;
import com.cybernode.projects.HotelBookingApp.entity.*;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.*;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.CheckoutService;
import com.cybernode.projects.HotelBookingApp.service.NotificationService;
import com.cybernode.projects.HotelBookingApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.model.PaymentIntent;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final NotificationService notificationService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${booking.expiry.minutes:10}")
    private Integer bookingExpiryMinutes;

    @Value("${cancellation.full-refund-days:7}")
    private Integer fullRefundDays;

    @Value("${cancellation.partial-refund-days:3}")
    private Integer partialRefundDays;

    @Value("${cancellation.partial-refund-percent:50}")
    private Integer partialRefundPercent;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {

        log.info("Initialising booking for hotel : {}, room: {}, date {}-{}", bookingRequest.getHotelId(),
                bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());

        // Validate date ranges: check-out date must be strictly after check-in date
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate()) ||
                bookingRequest.getCheckOutDate().isEqual(bookingRequest.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(() ->
                new ResourceNotFoundException("Hotel not found with id: "+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(() ->
                new ResourceNotFoundException("Room not found with id: "+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())+1;

        if (inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        // Reserve the room/ update the booked count of inventories
        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<Long> guestIdList) {
        log.info("Adding guests for booking with id: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found with id: "+bookingId));
        User user = getCurrentUser();

        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        if (booking.getBookingStatus() != BookingStatus.RESERVED) {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        List<Guest> guests = guestRepository.findAllById(guestIdList);
        for (Guest guest : guests) {
            if (!guest.getUser().equals(user)) {
                throw new UnAuthorisedException("Guest with id: "+guest.getId()+" does not belong to this user");
            }
        }

        booking.getGuests().addAll(guests);
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        if (hasBookingExpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "checkout.session.expired" -> handleCheckoutExpired(event);
            case "payment_intent.payment_failed" -> handlePaymentFailed(event);
            default -> log.warn("Unhandled event type: {}", event.getType());
        }
    }

    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        Booking booking = bookingRepository.findByPaymentSessionId(session.getId()).orElseThrow(() ->
                new ResourceNotFoundException("Booking not found for session ID: " + session.getId()));

        // Idempotency: if booking is already confirmed, ignore duplicate webhook events
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            log.info("Booking {} already confirmed, ignoring duplicate webhook event", booking.getId());
            return;
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Lock inventory and shift room count from reserved to booked
        inventoryRepository.getInventoryAndLockBeforeUpdate(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate());
        inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());
        notificationService.sendBookingConfirmation(booking);
    }

    private void handleCheckoutExpired(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) return;

        // Session expired without payment: release the held inventory and mark EXPIRED
        bookingRepository.findByPaymentSessionId(session.getId()).ifPresent(booking -> {
            if (booking.getBookingStatus() == BookingStatus.EXPIRED) {
                log.info("Booking {} already expired, ignoring duplicate webhook event", booking.getId());
                return;
            }
            if (booking.getBookingStatus() != BookingStatus.PAYMENTS_PENDING &&
                booking.getBookingStatus() != BookingStatus.GUESTS_ADDED &&
                booking.getBookingStatus() != BookingStatus.RESERVED) {
                log.info("Booking {} is in status {}, skipping session expiry webhook", booking.getId(), booking.getBookingStatus());
                return;
            }

            inventoryRepository.getInventoryAndLockBeforeUpdate(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate());
            inventoryRepository.releaseReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            log.info("Booking {} expired via Stripe session expiry, inventory released", booking.getId());
            notificationService.sendBookingExpired(booking);
        });
    }

    private void handlePaymentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent == null) return;

        // Retrieve bookingId from the payment intent metadata set during checkout session creation
        String bookingIdStr = paymentIntent.getMetadata().get("bookingId");
        if (bookingIdStr == null) {
            log.warn("PaymentIntent {} failed but no bookingId was found in metadata", paymentIntent.getId());
            return;
        }

        Long bookingId = Long.valueOf(bookingIdStr);
        bookingRepository.findById(bookingId).ifPresent(booking -> {
            if (booking.getBookingStatus() == BookingStatus.PAYMENT_FAILED) {
                log.info("Booking {} already marked as payment failed, ignoring duplicate webhook event", booking.getId());
                return;
            }
            if (booking.getBookingStatus() != BookingStatus.PAYMENTS_PENDING &&
                booking.getBookingStatus() != BookingStatus.GUESTS_ADDED &&
                booking.getBookingStatus() != BookingStatus.RESERVED) {
                log.info("Booking {} is in status {}, skipping payment failure webhook", booking.getId(), booking.getBookingStatus());
                return;
            }

            inventoryRepository.getInventoryAndLockBeforeUpdate(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate());
            inventoryRepository.releaseReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            booking.setBookingStatus(BookingStatus.PAYMENT_FAILED);
            bookingRepository.save(booking);
            log.info("Booking {} marked as PAYMENT_FAILED, inventory released", booking.getId());
            notificationService.sendPaymentFailed(booking);
        });
    }

    private BigDecimal calculateRefundAmount(Booking booking) {
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), booking.getCheckInDate());

        BigDecimal refundPercent;
        if (daysUntilCheckIn >= fullRefundDays) {
            refundPercent = BigDecimal.valueOf(100);
        } else if (daysUntilCheckIn >= partialRefundDays) {
            refundPercent = BigDecimal.valueOf(partialRefundPercent);
        } else {
            refundPercent = BigDecimal.ZERO;
        }

        return booking.getAmount()
                .multiply(refundPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        BigDecimal refundAmount = calculateRefundAmount(booking);

        booking.setBookingStatus(BookingStatus.REFUND_PENDING);
        booking.setRefundAmount(refundAmount);
        bookingRepository.save(booking);

        inventoryRepository.getInventoryAndLockBeforeUpdate(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());

        try {
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                Session session = Session.retrieve(booking.getPaymentSessionId());
                RefundCreateParams refundParams = RefundCreateParams.builder()
                        .setPaymentIntent(session.getPaymentIntent())
                        .setAmount(refundAmount.multiply(BigDecimal.valueOf(100)).longValue()) // Stripe wants cents
                        .build();

                Refund.create(refundParams);
            }
            booking.setBookingStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            notificationService.sendBookingCancelled(booking, refundAmount);
        } catch (StripeException e) {
            log.error("Refund failed for booking {}: {}", booking.getId(), e.getMessage());
            // booking stays REFUND_PENDING - needs manual reconciliation or a retry job
            throw new RuntimeException("Refund could not be processed, please contact support", e);
        }
    }

    @Override
    public List<BookingDto> getRefundPendingBookings() {
        return bookingRepository.findByBookingStatus(BookingStatus.REFUND_PENDING).stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        return booking.getBookingStatus();
    }

    private static final int MAX_PAGE_SIZE = 100;

    private Pageable clampPageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }

    @Override
    public Page<BookingDto> getAllBookingsByHotelId(Long hotelId, Pageable pageable) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Getting all booking for the hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        return bookingRepository.findByHotel(hotel, clampPageSize(pageable))
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Generating report for hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    @Override
    public Page<BookingDto> getMyBookings(Pageable pageable) {
        User user = getCurrentUser();
        return bookingRepository.findByUser(user, clampPageSize(pageable))
                .map(booking -> modelMapper.map(booking, BookingDto.class));
    }

    public boolean hasBookingExpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(bookingExpiryMinutes).isBefore(LocalDateTime.now());
    }

    // Scheduled sweep running every 5 minutes to expire abandoned/stale bookings
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void expireStaleBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(bookingExpiryMinutes);

        List<Booking> staleBookings = bookingRepository.findByBookingStatusInAndCreatedAtBefore(
                List.of(BookingStatus.RESERVED, BookingStatus.GUESTS_ADDED, BookingStatus.PAYMENTS_PENDING),
                cutoff);

        for (Booking booking : staleBookings) {
            log.info("Expiring stale booking with ID: {} (status was {})", booking.getId(), booking.getBookingStatus());

            // Lock inventory rows cleanly by ID and dates (using capacity-neutral locking query)
            inventoryRepository.getInventoryAndLockBeforeUpdate(booking.getRoom().getId(),
                    booking.getCheckInDate(), booking.getCheckOutDate());

            // Release the reserved rooms count
            inventoryRepository.releaseReservedInventory(booking.getRoom().getId(),
                    booking.getCheckInDate(), booking.getCheckOutDate(), booking.getRoomsCount());

            booking.setBookingStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            notificationService.sendBookingExpired(booking);
        }
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
