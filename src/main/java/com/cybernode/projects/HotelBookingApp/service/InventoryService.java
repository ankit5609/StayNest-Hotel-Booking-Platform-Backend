package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelPriceDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelSearchRequest;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    void deleteAllInventories(Room room);

}
