package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDto createReview(ReviewRequestDto requestDto);
    Page<ReviewDto> getReviewsForHotel(Long hotelId, Pageable pageable);
    ReviewDto updateReview(Long reviewId, ReviewUpdateDto updateDto);
    void deleteReview(Long reviewId);
    Page<ReviewDto> getMyReviews(Pageable pageable);
}
