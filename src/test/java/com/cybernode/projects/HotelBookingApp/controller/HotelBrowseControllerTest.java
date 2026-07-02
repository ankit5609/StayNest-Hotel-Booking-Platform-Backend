package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.HotelSearchRequest;
import com.cybernode.projects.HotelBookingApp.dto.NaturalLanguageSearchRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.NaturalLanguageSearchResponseDto;
import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.ConversationalSearchService;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.context.annotation.Import;
import com.cybernode.projects.HotelBookingApp.security.WebSecurityConfig;
import com.cybernode.projects.HotelBookingApp.security.JWTAuthFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HotelBrowseController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class HotelBrowseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private HotelService hotelService;

    @MockitoBean
    private ConversationalSearchService conversationalSearchService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    public void testSearchHotels_Success() throws Exception {
        when(inventoryService.searchHotels(any(HotelSearchRequest.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/hotels/search")
                        .param("city", "Mumbai")
                        .param("startDate", "2026-07-10")
                        .param("endDate", "2026-07-12")
                        .param("roomsCount", "2"))
                .andExpect(status().isOk());
    }

    @Test
    public void testSearchHotelsNaturalLanguage_Success() throws Exception {
        NaturalLanguageSearchRequestDto request = new NaturalLanguageSearchRequestDto();
        request.setQuery("cheap place in Mumbai next weekend");

        NaturalLanguageSearchResponseDto response = NaturalLanguageSearchResponseDto.builder()
                .interpretedQuery(new HotelSearchRequest())
                .missingFields(List.of())
                .results(Page.empty())
                .build();

        when(conversationalSearchService.search("cheap place in Mumbai next weekend")).thenReturn(response);

        mockMvc.perform(post("/hotels/search/nl")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.missingFields").isEmpty());
    }
}
