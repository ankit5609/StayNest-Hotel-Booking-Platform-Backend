package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.dto.GuestDto;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class HotelBookingController {

    private final BookingService bookingService;

    @PostMapping("/init")
    public ResponseEntity<BookingDto> initialiseBooking(@RequestBody BookingRequest bookingRequest) {
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
    }

    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDto> addGuests(@PathVariable Long bookingId,
                                                @RequestBody List<GuestDto> guestDtoList) {
        return ResponseEntity.ok(bookingService.addGuests(bookingId, guestDtoList));
    }

    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String, String>> initiatePayment(@PathVariable Long bookingId) {
        String sessionUrl = bookingService.initiatePayments(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/status")
    public ResponseEntity<Map<String, String>> getBookingStatus(@PathVariable Long bookingId) {
        return ResponseEntity.ok(Map.of("status", bookingService.getBookingStatus(bookingId)));
    }
}
