package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuestDto {
    // Unique guest identifier, optional during creation
    private Long id;

    // Guest's name cannot be blank
    @NotBlank(message = "Guest name is required")
    private String name;

    // Gender must be specified
    @NotNull(message = "Gender is required")
    private Gender gender;

    // Date of birth is required and must be in the past
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
}