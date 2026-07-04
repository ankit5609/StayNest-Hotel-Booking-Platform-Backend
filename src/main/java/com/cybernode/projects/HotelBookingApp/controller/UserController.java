package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.GuestDto;
import com.cybernode.projects.HotelBookingApp.dto.ProfileUpdateRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.UserDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelPriceResponseDto;
import com.cybernode.projects.HotelBookingApp.dto.ReviewDto;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.GuestService;
import com.cybernode.projects.HotelBookingApp.service.UserService;
import com.cybernode.projects.HotelBookingApp.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;
    private final GuestService guestService;
    private final ReviewService reviewService;

    @PatchMapping("/profile")
    @Operation(summary = "Update the user profile", tags = {"Profile"})
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequestDto profileUpdateRequestDto) {
        // ProfileUpdateRequestDto is validated (e.g. name length, past dateOfBirth) if those fields are present in the patch payload
        userService.updateProfile(profileUpdateRequestDto);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/profile/photo", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile photo", tags = {"Profile"})
    public ResponseEntity<String> uploadProfilePhoto(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String url = userService.uploadProfilePhoto(file);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/myBookings")
    @Operation(summary = "Get all my previous bookings", tags = {"Profile"})
    public ResponseEntity<Page<BookingDto>> getMyBookings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getMyBookings(pageable));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get my Profile", tags = {"Profile"})
    public ResponseEntity<UserDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/guests")
    @Operation(summary = "Get all my guests", tags = {"Booking Guests"})
    public ResponseEntity<Page<GuestDto>> getAllGuests(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(guestService.getAllGuests(pageable));
    }

    @PostMapping("/guests")
    @Operation(summary = "Add a new guest to my guests list", tags = {"Booking Guests"})
    public ResponseEntity<GuestDto> addNewGuest(@Valid @RequestBody GuestDto guestDto) {
        // GuestDto is validated (name, gender, dateOfBirth must be present and valid) before creation
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.addNewGuest(guestDto));
    }

    @PutMapping("guests/{guestId}")
    @Operation(summary = "Update a guest", tags = {"Booking Guests"})
    public ResponseEntity<Void> updateGuest(@PathVariable Long guestId, @Valid @RequestBody GuestDto guestDto) {
        // GuestDto is validated (name, gender, dateOfBirth must be present and valid) before updating
        guestService.updateGuest(guestId, guestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("guests/{guestId}")
    @Operation(summary = "Remove a guest", tags = {"Booking Guests"})
    public ResponseEntity<Void> deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myReviews")
    @Operation(summary = "Get all my submitted reviews", tags = {"Profile"})
    public ResponseEntity<Page<ReviewDto>> getMyReviews(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getMyReviews(pageable));
    }

    @PostMapping("/wishlist/{hotelId}")
    @Operation(summary = "Add a hotel to wishlist", tags = {"Profile"})
    public ResponseEntity<Void> addHotelToWishlist(@PathVariable Long hotelId) {
        userService.addHotelToWishlist(hotelId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/wishlist/{hotelId}")
    @Operation(summary = "Remove a hotel from wishlist", tags = {"Profile"})
    public ResponseEntity<Void> removeHotelFromWishlist(@PathVariable Long hotelId) {
        userService.removeHotelFromWishlist(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/wishlist")
    @Operation(summary = "Get my bookmarked hotels", tags = {"Profile"})
    public ResponseEntity<Page<HotelPriceResponseDto>> getWishlist(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.getWishlist(pageable));
    }
}
