package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
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

import com.cybernode.projects.HotelBookingApp.entity.HotelContactInfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "HOTEL_MANAGER")
    public void testCreateNewHotel_Success() throws Exception {
        HotelDto request = new HotelDto();
        request.setName("Luxury Resort");
        request.setCity("Pune");
        request.setContactInfo(new HotelContactInfo("123 Street", "9999999999", "hotel@test.com", "Pune"));

        HotelDto response = new HotelDto();
        response.setId(10L);
        response.setName("Luxury Resort");

        when(hotelService.createNewHotel(any(HotelDto.class))).thenReturn(response);

        mockMvc.perform(post("/admin/hotels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    @WithMockUser(roles = "GUEST")
    public void testCreateNewHotel_ForbiddenForGuest() throws Exception {
        HotelDto request = new HotelDto();
        request.setName("Luxury Resort");
        request.setCity("Pune");
        request.setContactInfo(new HotelContactInfo("123 Street", "9999999999", "hotel@test.com", "Pune"));

        mockMvc.perform(post("/admin/hotels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HOTEL_MANAGER")
    public void testGetHotelById_Success() throws Exception {
        HotelDto response = new HotelDto();
        response.setId(10L);
        response.setName("Luxury Resort");

        when(hotelService.getHotelById(10L)).thenReturn(response);

        mockMvc.perform(get("/admin/hotels/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    @WithMockUser(roles = "HOTEL_MANAGER")
    public void testDeleteHotelById_Success() throws Exception {
        doNothing().when(hotelService).deleteHotelById(10L);

        mockMvc.perform(delete("/admin/hotels/10")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "HOTEL_MANAGER")
    public void testSettleRefund_Success() throws Exception {
        doNothing().when(bookingService).settleRefund(100L);

        mockMvc.perform(post("/admin/hotels/bookings/100/refund")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
