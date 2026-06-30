package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewRequestDto;
import com.cybernode.projects.HotelBookingApp.entity.Booking;
import com.cybernode.projects.HotelBookingApp.entity.Review;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.BookingRepository;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.ReviewRepository;
import com.cybernode.projects.HotelBookingApp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ReviewDto createReview(ReviewRequestDto requestDto) {
        log.info("Creating review for booking with ID: {}", requestDto.getBookingId());
        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(requestDto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + requestDto.getBookingId()));

        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new UnAuthorisedException("You can only review your own bookings");
        }

        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be reviewed");
        }

        if (!booking.getCheckOutDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("You can only review a stay after checkout");
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("This booking has already been reviewed");
        }

        Review review = Review.builder()
                .booking(booking)
                .hotel(booking.getHotel())
                .user(currentUser)
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .build();

        review = reviewRepository.save(review);

        // Recalculate average rating and review count on the hotel
        hotelRepository.recalculateRating(booking.getHotel().getId());

        ReviewDto dto = modelMapper.map(review, ReviewDto.class);
        dto.setGuestName(getMaskedName(currentUser.getName()));
        return dto;
    }

    @Override
    public Page<ReviewDto> getReviewsForHotel(Long hotelId, Pageable pageable) {
        log.info("Fetching reviews for hotel with ID: {}", hotelId);
        Page<Review> reviews = reviewRepository.findByHotelId(hotelId, pageable);
        return reviews.map(review -> {
            ReviewDto dto = modelMapper.map(review, ReviewDto.class);
            dto.setGuestName(getMaskedName(review.getUser().getName()));
            return dto;
        });
    }

    private String getMaskedName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Guest";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
