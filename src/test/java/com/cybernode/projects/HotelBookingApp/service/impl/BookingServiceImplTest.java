package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.entity.Booking;
import com.cybernode.projects.HotelBookingApp.entity.Guest;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.*;
import com.cybernode.projects.HotelBookingApp.service.CheckoutService;
import com.cybernode.projects.HotelBookingApp.service.NotificationService;
import com.cybernode.projects.HotelBookingApp.strategy.PricingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private PricingService pricingService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User otherUser;
    private Booking booking;
    private Guest guest;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@test.com");

        booking = new Booking();
        booking.setId(100L);
        booking.setUser(user);
        booking.setBookingStatus(BookingStatus.RESERVED);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setGuests(new HashSet<>());

        guest = new Guest();
        guest.setId(5L);
        guest.setUser(user);

        ReflectionTestUtils.setField(bookingService, "bookingExpiryMinutes", 10);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User principal) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testAddGuests_Success() {
        mockSecurityContext(user);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(guestRepository.findAllById(List.of(5L))).thenReturn(List.of(guest));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto dto = new BookingDto();
        dto.setId(100L);
        when(modelMapper.map(any(Booking.class), eq(BookingDto.class))).thenReturn(dto);

        BookingResultWrapper();
    }

    private void BookingResultWrapper() {
        BookingDto result = bookingService.addGuests(100L, List.of(5L));
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    public void testAddGuests_UnauthorisedBooking() {
        mockSecurityContext(otherUser);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThrows(UnAuthorisedException.class, () -> bookingService.addGuests(100L, List.of(5L)));
    }

    @Test
    public void testAddGuests_UnauthorisedGuest() {
        mockSecurityContext(user);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        
        Guest otherGuest = new Guest();
        otherGuest.setId(6L);
        otherGuest.setUser(otherUser);
        when(guestRepository.findAllById(List.of(6L))).thenReturn(List.of(otherGuest));

        assertThrows(UnAuthorisedException.class, () -> bookingService.addGuests(100L, List.of(6L)));
    }

    @Test
    public void testInitiatePayments_Success() {
        mockSecurityContext(user);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(checkoutService.getCheckoutSession(eq(booking), anyString(), anyString())).thenReturn("http://checkout-url");

        String url = bookingService.initiatePayments(100L);

        assertEquals("http://checkout-url", url);
        assertEquals(BookingStatus.PAYMENTS_PENDING, booking.getBookingStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    public void testInitiatePayments_Unauthorised() {
        mockSecurityContext(otherUser);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThrows(UnAuthorisedException.class, () -> bookingService.initiatePayments(100L));
    }
}
