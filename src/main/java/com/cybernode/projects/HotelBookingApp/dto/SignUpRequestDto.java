package com.cybernode.projects.HotelBookingApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequestDto {
    // Email field must be a valid email format and not blank
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Password must be not blank and at least 8 characters long
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // User name is required and cannot be blank
    @NotBlank(message = "Name is required")
    private String name;
}
