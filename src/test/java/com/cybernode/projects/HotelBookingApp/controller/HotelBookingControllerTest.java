package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.dto.BookingPaymentInitResponseDto;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelBookingController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class HotelBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    public void testInitialiseBooking_Success() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setHotelId(100L);
        request.setRoomId(10L);
        request.setCheckInDate(LocalDate.now().plusDays(5));
        request.setCheckOutDate(LocalDate.now().plusDays(7));
        request.setRoomsCount(1);

        BookingDto response = new BookingDto();
        response.setId(100L);
        response.setRoomsCount(1);

        when(bookingService.initialiseBooking(any(BookingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/bookings/init")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100L));
    }

    @Test
    @WithMockUser
    public void testAddGuests_Success() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(100L);

        when(bookingService.addGuests(eq(100L), any(List.class))).thenReturn(response);

        mockMvc.perform(post("/bookings/100/addGuests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[5, 6]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100L));
    }

    @Test
    @WithMockUser
    public void testInitiatePayment_Success() throws Exception {
        when(bookingService.initiatePayments(100L)).thenReturn("http://session-url");

        mockMvc.perform(post("/bookings/100/payments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionUrl").value("http://session-url"));
    }

    @Test
    @WithMockUser
    public void testGetBookingDetails_Success() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(100L);

        when(bookingService.getBookingDetails(100L)).thenReturn(response);

        mockMvc.perform(get("/bookings/100")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100L));
    }
}
