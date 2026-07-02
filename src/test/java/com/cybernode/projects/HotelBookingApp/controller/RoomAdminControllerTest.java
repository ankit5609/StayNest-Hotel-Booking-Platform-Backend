package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.RoomDto;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.RoomService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;
import com.cybernode.projects.HotelBookingApp.security.WebSecurityConfig;
import com.cybernode.projects.HotelBookingApp.security.JWTAuthFilter;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomAdminController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class RoomAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "HOTEL_MANAGER")
    public void testCreateNewRoom_Success() throws Exception {
        RoomDto request = new RoomDto();
        request.setType("Deluxe Suite");
        request.setBasePrice(BigDecimal.valueOf(150.00));
        request.setTotalCount(5);
        request.setCapacity(2);

        RoomDto response = new RoomDto();
        response.setId(50L);
        response.setType("Deluxe Suite");

        when(roomService.createNewRoom(eq(10L), any(RoomDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/hotels/10/rooms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(50L));
    }

    @Test
    @WithMockUser(roles = "HOTEL_MANAGER")
    public void testGetAllRoomsInHotel_Success() throws Exception {
        RoomDto room1 = new RoomDto();
        room1.setId(50L);
        when(roomService.getAllRoomsInHotel(10L)).thenReturn(List.of(room1));

        mockMvc.perform(get("/admin/hotels/10/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(50L));
    }
}
