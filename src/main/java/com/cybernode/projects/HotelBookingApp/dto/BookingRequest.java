package com.cybernode.projects.HotelBookingApp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    // Hotel ID must be specified for a booking
    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    // Room ID must be specified for a booking
    @NotNull(message = "Room ID is required")
    private Long roomId;

    // Check-in date is required and must be today or in the future
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date cannot be in the past")
    private LocalDate checkInDate;

    // Check-out date is required and must be strictly in the future
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    // At least 1 room must be booked and cannot be negative/zero
    @NotNull(message = "Rooms count is required")
    @Min(value = 1, message = "At least 1 room must be booked")
    private Integer roomsCount;
}
