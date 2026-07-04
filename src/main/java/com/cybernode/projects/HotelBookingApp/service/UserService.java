package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.ProfileUpdateRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelPriceResponseDto;
import com.cybernode.projects.HotelBookingApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();

    String uploadProfilePhoto(MultipartFile file);

    void addHotelToWishlist(Long hotelId);

    void removeHotelFromWishlist(Long hotelId);

    Page<HotelPriceResponseDto> getWishlist(Pageable pageable);
}
