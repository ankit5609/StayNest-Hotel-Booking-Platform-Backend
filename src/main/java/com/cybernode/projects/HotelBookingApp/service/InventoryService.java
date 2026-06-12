package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.entity.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteFutureInventories(Room room);

}
