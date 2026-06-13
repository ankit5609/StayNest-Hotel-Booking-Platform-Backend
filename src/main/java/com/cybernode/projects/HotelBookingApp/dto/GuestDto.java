package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
