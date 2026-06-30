package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewRequestDto;
import com.cybernode.projects.HotelBookingApp.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @Operation(summary = "Submit a new review for a booking", tags = {"Reviews"})
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewRequestDto requestDto) {
        ReviewDto reviewDto = reviewService.createReview(requestDto);
        return new ResponseEntity<>(reviewDto, HttpStatus.CREATED);
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    @Operation(summary = "Get paginated reviews for a hotel", tags = {"Reviews"})
    public ResponseEntity<Page<ReviewDto>> getHotelReviews(@PathVariable Long hotelId, Pageable pageable) {
        Page<ReviewDto> reviews = reviewService.getReviewsForHotel(hotelId, pageable);
        return ResponseEntity.ok(reviews);
    }
}
