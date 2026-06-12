package com.cybernode.projects.HotelBookingApp.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class HotelContactInfo {
    private String address;
    private String phoneNumber;
    private String email;
    private String location;
}
