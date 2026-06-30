package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.enums.Gender;
import com.cybernode.projects.HotelBookingApp.enums.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private  Gender gender;
    private LocalDate dateOfBirth;
    private Set<Role> roles;
}
