package com.cybernode.projects.HotelBookingApp.security;

import com.cybernode.projects.HotelBookingApp.dto.ForgotPasswordRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.ResetPasswordRequestDto;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.repository.UserRepository;
import com.cybernode.projects.HotelBookingApp.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@test.com");
        user.setPassword("old_encoded_password");
        user.setTokenVersion(0);
    }

    @Test
    public void testForgotPassword_Success() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto();
        request.setEmail("john@test.com");

        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.forgotPassword(request);

        assertNotNull(user.getPasswordResetToken());
        assertNotNull(user.getPasswordResetTokenExpiresAt());
        assertTrue(user.getPasswordResetTokenExpiresAt().isAfter(LocalDateTime.now()));

        verify(userRepository, times(1)).save(user);
        verify(notificationService, times(1)).sendPasswordResetEmail(eq("john@test.com"), anyString());
    }

    @Test
    public void testForgotPassword_UserNotFound() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto();
        request.setEmail("unknown@test.com");

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.forgotPassword(request));

        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    public void testResetPassword_Success() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setToken("reset-token-123");
        request.setNewPassword("NewPassword123!");

        user.setPasswordResetToken("reset-token-123");
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByPasswordResetToken("reset-token-123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.resetPassword(request);

        assertEquals("new_encoded_password", user.getPassword());
        assertNull(user.getPasswordResetToken());
        assertNull(user.getPasswordResetTokenExpiresAt());
        assertEquals(1, user.getTokenVersion()); // token version incremented to revoke active JWT tokens

        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testResetPassword_InvalidToken() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setToken("invalid-token");
        request.setNewPassword("NewPassword123!");

        when(userRepository.findByPasswordResetToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testResetPassword_ExpiredToken() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setToken("expired-token");
        request.setNewPassword("NewPassword123!");

        user.setPasswordResetToken("expired-token");
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().minusMinutes(5)); // expired 5 mins ago

        when(userRepository.findByPasswordResetToken("expired-token")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));

        verify(userRepository, never()).save(any(User.class));
    }
}
