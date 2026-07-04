package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class NaturalLanguageSearchRequestDto {
    @NotBlank
    private String query;

    // existing search context passed from the frontend for multi-turn conversations
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomsCount;
    private Integer adults;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private SortOption sortBy;
}
