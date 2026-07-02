package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.ProfileUpdateRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();

    String uploadProfilePhoto(org.springframework.web.multipart.MultipartFile file);

    void addHotelToWishlist(Long hotelId);

    void removeHotelFromWishlist(Long hotelId);

    org.springframework.data.domain.Page<com.cybernode.projects.HotelBookingApp.dto.HotelDto> getWishlist(org.springframework.data.domain.Pageable pageable);
}
