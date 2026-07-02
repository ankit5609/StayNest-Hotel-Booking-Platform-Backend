package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewRequestDto;
import com.cybernode.projects.HotelBookingApp.entity.Booking;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.Review;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.BookingRepository;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.ReviewRepository;
import com.cybernode.projects.HotelBookingApp.service.ReviewEmbeddingService;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ReviewEmbeddingService reviewEmbeddingService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User user;
    private User otherUser;
    private Booking booking;
    private Hotel hotel;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Jane Smith");

        hotel = new Hotel();
        hotel.setId(10L);

        booking = new Booking();
        booking.setId(100L);
        booking.setUser(user);
        booking.setHotel(hotel);
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setCheckOutDate(LocalDate.now().minusDays(1));
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
    public void testCreateReview_Success() {
        mockSecurityContext(user);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(100L)).thenReturn(false);

        ReviewRequestDto request = new ReviewRequestDto();
        request.setBookingId(100L);
        request.setRating(5);
        request.setComment("Great stay!");

        Review review = new Review();
        review.setId(200L);
        review.setBooking(booking);
        review.setHotel(hotel);
        review.setUser(user);
        review.setRating(5);
        review.setComment("Great stay!");

        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDto dto = new ReviewDto();
        dto.setId(200L);
        when(modelMapper.map(any(Review.class), eq(ReviewDto.class))).thenReturn(dto);

        ReviewDto result = reviewService.createReview(request);

        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(hotelRepository, times(1)).recalculateRating(10L);
        verify(reviewEmbeddingService, times(1)).indexReview(any(Review.class));
    }

    @Test
    public void testCreateReview_Unauthorised() {
        mockSecurityContext(otherUser);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        ReviewRequestDto request = new ReviewRequestDto();
        request.setBookingId(100L);

        assertThrows(UnAuthorisedException.class, () -> reviewService.createReview(request));
    }

    @Test
    public void testCreateReview_FutureStay() {
        mockSecurityContext(user);
        booking.setCheckOutDate(LocalDate.now().plusDays(2));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        ReviewRequestDto request = new ReviewRequestDto();
        request.setBookingId(100L);

        assertThrows(IllegalStateException.class, () -> reviewService.createReview(request));
    }
}
