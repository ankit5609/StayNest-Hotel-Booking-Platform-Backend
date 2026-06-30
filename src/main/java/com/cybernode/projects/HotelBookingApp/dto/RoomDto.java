package com.cybernode.projects.HotelBookingApp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomDto {
    // Unique identifier of the room type, optional on creation
    private Long id;

    // Room type (e.g., Deluxe, Suite) must not be blank
    @NotBlank(message = "Room type is required")
    private String type;

    // Base price per night cannot be negative
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price cannot be negative")
    private BigDecimal basePrice;

    // Optional list of room photos
    private String[] photos;

    // Optional list of room amenities
    private String[] amenities;

    // Total rooms available of this type in the hotel must be at least 1
    @NotNull(message = "Total count is required")
    @Min(value = 1, message = "Total room count must be at least 1")
    private Integer totalCount;

    // Room capacity (number of guests allowed) must be at least 1
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1 guest")
    private Integer capacity;
}
