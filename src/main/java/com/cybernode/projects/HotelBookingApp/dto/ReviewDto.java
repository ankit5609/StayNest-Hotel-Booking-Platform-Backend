package com.cybernode.projects.HotelBookingApp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String guestName;
    private Long hotelId;
}
