package com.cybernode.projects.HotelBookingApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NaturalLanguageSearchRequestDto {
    @NotBlank
    private String query;
}
