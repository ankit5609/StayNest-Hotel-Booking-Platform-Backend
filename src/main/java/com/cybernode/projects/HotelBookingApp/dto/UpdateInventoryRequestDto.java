package com.cybernode.projects.HotelBookingApp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateInventoryRequestDto {
    // Start date is required for the inventory update range
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    // End date is required for the inventory update range
    @NotNull(message = "End date is required")
    private LocalDate endDate;

    // Surge factor is required and must be between 0 (exclusive) and 10.0 (inclusive)
    @NotNull(message = "Surge factor is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Surge factor must be greater than 0")
    @DecimalMax(value = "10.0", message = "Surge factor cannot exceed 10.0")
    private BigDecimal surgeFactor;

    // Closed flag is required to open/close inventories
    @NotNull(message = "Closed status is required")
    private Boolean closed;
}
