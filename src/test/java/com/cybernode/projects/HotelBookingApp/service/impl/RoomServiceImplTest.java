package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.RoomDto;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.RoomRepository;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private User owner;
    private User nonOwner;
    private Hotel hotel;
    private Room room;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setId(1L);

        nonOwner = new User();
        nonOwner.setId(2L);

        hotel = new Hotel();
        hotel.setId(10L);
        hotel.setOwner(owner);
        hotel.setActive(true);

        room = new Room();
        room.setId(50L);
        room.setHotel(hotel);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(User principal) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testCreateNewRoom_Success() {
        mockSecurityContext(owner);
        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));

        RoomDto inputDto = new RoomDto();
        inputDto.setType("Deluxe");

        when(modelMapper.map(inputDto, Room.class)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);

        RoomDto outputDto = new RoomDto();
        outputDto.setId(50L);
        outputDto.setType("Deluxe");
        when(modelMapper.map(room, RoomDto.class)).thenReturn(outputDto);

        RoomDto result = roomService.createNewRoom(10L, inputDto);

        assertNotNull(result);
        assertEquals(50L, result.getId());
        verify(roomRepository, times(1)).save(room);
        verify(inventoryService, times(1)).initializeRoomForAYear(room);
    }

    @Test
    public void testCreateNewRoom_Unauthorised() {
        mockSecurityContext(nonOwner);
        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));

        RoomDto inputDto = new RoomDto();

        assertThrows(UnAuthorisedException.class, () -> roomService.createNewRoom(10L, inputDto));
    }
}
