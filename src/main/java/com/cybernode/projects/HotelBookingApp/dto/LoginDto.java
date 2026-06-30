package com.cybernode.projects.HotelBookingApp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    // Email is required to log in and must be properly formatted
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Password is required to log in
    @NotBlank(message = "Password is required")
    private String password;
}
