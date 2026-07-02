package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.exception.UnAuthorisedException;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.InventoryRepository;
import com.cybernode.projects.HotelBookingApp.repository.RoomRepository;
import com.cybernode.projects.HotelBookingApp.service.ImageUploadService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private User owner;
    private User nonOwner;
    private Hotel hotel;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@test.com");

        nonOwner = new User();
        nonOwner.setId(2L);
        nonOwner.setEmail("nonowner@test.com");

        hotel = new Hotel();
        hotel.setId(10L);
        hotel.setName("Test Hotel");
        hotel.setOwner(owner);
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
    public void testGetHotelById_Success() {
        mockSecurityContext(owner);
        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));
        
        HotelDto dto = new HotelDto();
        dto.setId(10L);
        dto.setName("Test Hotel");
        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(dto);

        HotelDto result = hotelService.getHotelById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(hotelRepository, times(1)).findById(10L);
    }

    @Test
    public void testGetHotelById_Unauthorised() {
        mockSecurityContext(nonOwner);
        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));

        assertThrows(UnAuthorisedException.class, () -> hotelService.getHotelById(10L));
    }

    @Test
    public void testGetHotelById_NotFound() {
        when(hotelRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.getHotelById(10L));
    }

    @Test
    public void testUpdateHotelById_Success() {
        mockSecurityContext(owner);
        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(hotel)).thenReturn(hotel);
        doNothing().when(modelMapper).map(any(HotelDto.class), any(Hotel.class));

        HotelDto inputDto = new HotelDto();
        inputDto.setName("Updated Hotel Name");

        HotelDto outputDto = new HotelDto();
        outputDto.setId(10L);
        outputDto.setName("Updated Hotel Name");

        when(modelMapper.map(hotel, HotelDto.class)).thenReturn(outputDto);

        HotelDto result = hotelService.updateHotelById(10L, inputDto);

        assertNotNull(result);
        assertEquals("Updated Hotel Name", result.getName());
        verify(modelMapper, times(1)).map(inputDto, hotel);
        verify(hotelRepository, times(1)).save(hotel);
    }

    @Test
    public void testUpdateHotelById_Unauthorised() {
        mockSecurityContext(nonOwner);
        when(hotelRepository.findById(10L)).thenReturn(Optional.of(hotel));

        HotelDto inputDto = new HotelDto();

        assertThrows(UnAuthorisedException.class, () -> hotelService.updateHotelById(10L, inputDto));
    }
}
