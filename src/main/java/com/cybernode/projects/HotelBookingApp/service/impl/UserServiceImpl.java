package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.ProfileUpdateRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.repository.UserRepository;
import com.cybernode.projects.HotelBookingApp.service.ImageUploadService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ImageUploadService imageUploadService;
    private final HotelRepository hotelRepository;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+id));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();

        if(profileUpdateRequestDto.getDateOfBirth() != null) user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        if(profileUpdateRequestDto.getGender() != null) user.setGender(profileUpdateRequestDto.getGender());
        if (profileUpdateRequestDto.getName() != null) user.setName(profileUpdateRequestDto.getName());
        if (profileUpdateRequestDto.getAvatarUrl() != null) user.setAvatarUrl(profileUpdateRequestDto.getAvatarUrl());

        userRepository.save(user);
    }

    @Override
    public UserDto getMyProfile() {
        User user = getCurrentUser();
        log.info("Getting the profile for user with id: {}", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public String uploadProfilePhoto(MultipartFile file) {
        log.info("Uploading profile photo for current user");
        User user = getCurrentUser();
        String imageUrl = imageUploadService.uploadImage(file);
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    @Override
    @Transactional
    public void addHotelToWishlist(Long hotelId) {
        log.info("Adding hotel with ID {} to user wishlist", hotelId);
        User user = getCurrentUser();
        User fullUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(
                () -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId)
        );
        fullUser.getWishlist().add(hotel);
        userRepository.save(fullUser);
    }

    @Override
    @Transactional
    public void removeHotelFromWishlist(Long hotelId) {
        log.info("Removing hotel with ID {} from user wishlist", hotelId);
        User user = getCurrentUser();
        User fullUser = userRepository.findById(user.getId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(
                () -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId)
        );
        fullUser.getWishlist().remove(hotel);
        userRepository.save(fullUser);
    }

    @Override
    public Page<HotelDto> getWishlist(Pageable pageable) {
        log.info("Fetching wishlist for current user");
        User user = getCurrentUser();
        Page<Hotel> hotels = hotelRepository.findWishlistByUserId(user.getId(), pageable);
        return hotels.map(hotel -> modelMapper.map(hotel, HotelDto.class));
    }
}
