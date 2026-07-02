package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.HotelQaResponseDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewUpdateDto;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.HotelQaService;
import com.cybernode.projects.HotelBookingApp.service.ReviewService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private HotelQaService hotelQaService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    public void testSubmitReview_Success() throws Exception {
        ReviewRequestDto request = new ReviewRequestDto();
        request.setBookingId(100L);
        request.setRating(5);
        request.setComment("Excellent service");

        ReviewDto responseDto = new ReviewDto();
        responseDto.setId(200L);
        responseDto.setRating(5);
        responseDto.setComment("Excellent service");

        when(reviewService.createReview(any(ReviewRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(200L))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @WithMockUser
    public void testSubmitReview_ValidationFailure() throws Exception {
        ReviewRequestDto request = new ReviewRequestDto();
        request.setBookingId(100L);
        request.setRating(6); // Invalid rating (max 5)

        mockMvc.perform(post("/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAskAboutHotel() throws Exception {
        HotelQaResponseDto response = HotelQaResponseDto.builder()
                .answer("Yes, the pool is open.")
                .sourceReviewIds(List.of(1L, 2L))
                .build();

        when(hotelQaService.ask(10L, "Is the pool open?")).thenReturn(response);

        mockMvc.perform(get("/hotels/10/ask")
                        .param("question", "Is the pool open?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").value("Yes, the pool is open."))
                .andExpect(jsonPath("$.data.sourceReviewIds[0]").value(1L));
    }

    @Test
    @WithMockUser
    public void testUpdateReview_Success() throws Exception {
        ReviewUpdateDto request = new ReviewUpdateDto();
        request.setRating(4);
        request.setComment("Updated comment");

        ReviewDto responseDto = new ReviewDto();
        responseDto.setId(200L);
        responseDto.setRating(4);
        responseDto.setComment("Updated comment");

        when(reviewService.updateReview(eq(200L), any(ReviewUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/reviews/200")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    @WithMockUser
    public void testDeleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(200L);

        mockMvc.perform(delete("/reviews/200")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
