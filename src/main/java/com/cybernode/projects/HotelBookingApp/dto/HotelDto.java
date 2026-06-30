package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.entity.HotelContactInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HotelDto {
    // Unique identifier of the hotel, optional during creation
    private Long id;

    // Hotel name cannot be blank
    @NotBlank(message = "Hotel name is required")
    private String name;

    // City where the hotel is located cannot be blank
    @NotBlank(message = "City is required")
    private String city;

    // Optional list of hotel photos
    private String[] photos;

    // Optional list of hotel amenities
    private String[] amenities;

    // Contact info is required and its fields must be validated
    @NotNull(message = "Contact info is required")
    @Valid
    private HotelContactInfo contactInfo;

    // Flag indicating whether the hotel is active or not
    private Boolean active;

    private Double averageRating;
    private Long reviewCount;
}
