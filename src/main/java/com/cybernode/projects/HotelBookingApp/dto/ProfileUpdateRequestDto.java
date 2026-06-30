package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    // Optional field: if provided, name must have at least 2 characters
    @Size(min = 2, message = "Name must be at least 2 characters")
    private String name;

    // Optional field: if provided, date of birth must be in the past
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // Optional field representing user gender
    private Gender gender;
}
