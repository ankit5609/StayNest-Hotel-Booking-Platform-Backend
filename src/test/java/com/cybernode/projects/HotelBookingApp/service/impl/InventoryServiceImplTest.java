package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import com.cybernode.projects.HotelBookingApp.repository.HotelMinPriceRepository;
import com.cybernode.projects.HotelBookingApp.repository.InventoryRepository;
import com.cybernode.projects.HotelBookingApp.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private HotelMinPriceRepository hotelMinPriceRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Room room;
    private Hotel hotel;

    @BeforeEach
    public void setUp() {
        hotel = new Hotel();
        hotel.setId(10L);
        hotel.setCity("Mumbai");

        room = new Room();
        room.setId(50L);
        room.setHotel(hotel);
        room.setBasePrice(BigDecimal.valueOf(100.00));
        room.setTotalCount(5);
    }

    @Test
    public void testInitializeRoomForAYear() {
        inventoryService.initializeRoomForAYear(room);

        verify(inventoryRepository, atLeast(365)).save(any(Inventory.class));
    }
}
