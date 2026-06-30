package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelInfoDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelInfoRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto);

    Page<HotelDto> getAllHotels(Pageable pageable);

    String uploadHotelPhoto(Long hotelId, MultipartFile file);
}
