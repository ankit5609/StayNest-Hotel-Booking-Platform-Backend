package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.ProfileUpdateRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();

    String uploadProfilePhoto(org.springframework.web.multipart.MultipartFile file);
}
