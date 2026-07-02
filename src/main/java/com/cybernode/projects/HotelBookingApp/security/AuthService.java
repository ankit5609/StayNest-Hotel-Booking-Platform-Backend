package com.cybernode.projects.HotelBookingApp.security;

import com.cybernode.projects.HotelBookingApp.dto.AuthTokensDTO;
import com.cybernode.projects.HotelBookingApp.dto.LoginDto;
import com.cybernode.projects.HotelBookingApp.dto.SignUpRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.Role;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import com.cybernode.projects.HotelBookingApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cybernode.projects.HotelBookingApp.dto.ForgotPasswordRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.ResetPasswordRequestDto;
import com.cybernode.projects.HotelBookingApp.service.NotificationService;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final NotificationService notificationService;

    public UserDto signUp(SignUpRequestDto signUpRequestDto) {

        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if (user != null) {
            throw new RuntimeException("User is already present with same email id");
        }

        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDto.class);
    }

    public AuthTokensDTO login(LoginDto dto) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                dto.getEmail(),
                                dto.getPassword()
                        )
                );
        User user = (User) authentication.getPrincipal();
        String accessToken =
                jwtService.generateAccessToken(user);
        String refreshToken =
                jwtService.generateRefreshToken(user);
        return new AuthTokensDTO(
                accessToken,
                refreshToken,
                user.getRoles()
        );
    }

    public AuthTokensDTO refreshToken(String refreshToken) {
        Long id = jwtService.getUserIdFromToken(refreshToken);
        Integer tokenVersion = jwtService.getTokenVersionFromToken(refreshToken);

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: "+id));

        // Defensively check for null values to prevent NullPointerExceptions on legacy accounts
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        if (tokenVersion == null || !tokenVersion.equals(currentVersion)) {
            throw new JwtException("Refresh token has been revoked");
        }

        String accessToken = jwtService.generateAccessToken(user);
        return new AuthTokensDTO(
                accessToken,
                "",
                user.getRoles()
        );
    }

    public void logout(User user) {
        // Increment tokenVersion to invalidate all issued refresh tokens on the server
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);
        userRepository.save(user);
    }

    public void forgotPassword(ForgotPasswordRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + dto.getEmail()));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        notificationService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void resetPassword(ResetPasswordRequestDto dto) {
        User user = userRepository.findByPasswordResetToken(dto.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiresAt() == null || user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        // Force log out of all active devices/sessions upon password reset
        int currentVersion = user.getTokenVersion() != null ? user.getTokenVersion() : 0;
        user.setTokenVersion(currentVersion + 1);

        userRepository.save(user);
    }
}
