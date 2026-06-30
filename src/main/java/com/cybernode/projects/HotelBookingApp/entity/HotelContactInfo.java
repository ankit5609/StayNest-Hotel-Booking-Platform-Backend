package com.cybernode.projects.HotelBookingApp.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Embeddable
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HotelContactInfo {
    // Hotel physical address must not be blank
    @NotBlank(message = "Address is required")
    private String address;

    // Phone number must not be blank
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    // Contact email must be a valid email format
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // GPS coordinates or Google Map link/location string
    private String location;
}
