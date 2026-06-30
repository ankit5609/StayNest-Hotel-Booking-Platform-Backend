package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HotelSearchRequest {
    private String city;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer roomsCount;

    private Integer page = 0;
    private Integer size = 10;

    // optional filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private SortOption sortBy = SortOption.PRICE_ASC;
}
