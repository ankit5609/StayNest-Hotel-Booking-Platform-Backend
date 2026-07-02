package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.AuthTokensDTO;
import com.cybernode.projects.HotelBookingApp.dto.LoginDto;
import com.cybernode.projects.HotelBookingApp.dto.SignUpRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.enums.Role;
import com.cybernode.projects.HotelBookingApp.security.AuthService;
import com.cybernode.projects.HotelBookingApp.security.JWTService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;
import com.cybernode.projects.HotelBookingApp.security.WebSecurityConfig;
import com.cybernode.projects.HotelBookingApp.security.JWTAuthFilter;

import com.cybernode.projects.HotelBookingApp.dto.ForgotPasswordRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.ResetPasswordRequestDto;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({WebSecurityConfig.class, JWTAuthFilter.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    public void testSignup_Success() throws Exception {
        SignUpRequestDto request = new SignUpRequestDto();
        request.setName("John Doe");
        request.setEmail("john@test.com");
        request.setPassword("Password123!");

        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("John Doe");
        response.setEmail("john@test.com");

        when(authService.signUp(any(SignUpRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("John Doe"));
    }

    @Test
    public void testLogin_Success() throws Exception {
        LoginDto request = new LoginDto();
        request.setEmail("john@test.com");
        request.setPassword("Password123!");

        AuthTokensDTO tokens = new AuthTokensDTO("access_token_123", "refresh_token_123", Set.of(Role.GUEST));
        when(authService.login(any(LoginDto.class))).thenReturn(tokens);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access_token_123"));
    }

    @Test
    public void testForgotPassword_Success() throws Exception {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto();
        request.setEmail("john@test.com");

        doNothing().when(authService).forgotPassword(any(ForgotPasswordRequestDto.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Password reset email sent successfully"));
    }

    @Test
    public void testResetPassword_Success() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setToken("reset-token-123");
        request.setNewPassword("NewPassword123!");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequestDto.class));

        mockMvc.perform(post("/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Password reset successfully"));
    }
}
