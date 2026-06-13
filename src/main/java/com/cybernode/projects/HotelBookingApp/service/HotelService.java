package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelInfoDto;

public interface HotelService {
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);
}