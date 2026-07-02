package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.ProfileUpdateRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.GuestService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;
import com.cybernode.projects.HotelBookingApp.security.WebSecurityConfig;
import com.cybernode.projects.HotelBookingApp.security.JWTAuthFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private GuestService guestService;

    @MockitoBean
    private JWTService jwtService;

    @Test
    @WithMockUser
    public void testGetMyProfile_Success() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John Doe");

        when(userService.getMyProfile()).thenReturn(dto);

        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    @WithMockUser
    public void testUpdateProfile_Success() throws Exception {
        doNothing().when(userService).updateProfile(any(ProfileUpdateRequestDto.class));

        ProfileUpdateRequestDto request = new ProfileUpdateRequestDto();
        request.setName("Jane Doe");

        mockMvc.perform(patch("/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    public void testGetMyBookings_Success() throws Exception {
        when(bookingService.getMyBookings(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/users/myBookings"))
                .andExpect(status().isOk());
    }
}
