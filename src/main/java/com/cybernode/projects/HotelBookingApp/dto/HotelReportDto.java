package com.cybernode.projects.HotelBookingApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelReportDto {
    private Long totalConfirmedBookings;
    private BigDecimal totalRevenueOfConfirmedBookings;
    private BigDecimal avgRevenue;
}
