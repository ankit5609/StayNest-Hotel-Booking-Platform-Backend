package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDto createReview(ReviewRequestDto requestDto);
    Page<ReviewDto> getReviewsForHotel(Long hotelId, Pageable pageable);
}
