package com.cybernode.projects.HotelBookingApp.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelQaResponseDto {
    private String answer;
    private List<Long> sourceReviewIds;
}
