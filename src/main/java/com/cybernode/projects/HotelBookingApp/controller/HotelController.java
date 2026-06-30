package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelReportDto;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new hotel", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> createNewHotel(@Valid @RequestBody HotelDto hotelDto) {
        // HotelDto is validated against constraints (not blank fields, valid contactInfo fields) before creation
        log.info("Attempting to create a new hotel with name: "+hotelDto.getName());
        HotelDto hotel = hotelService.createNewHotel(hotelDto);
        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    @Operation(summary = "Get a hotel by Id", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId) {
        HotelDto hotelDto = hotelService.getHotelById(hotelId);
        return ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{hotelId}")
    @Operation(summary = "Update a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId, @Valid @RequestBody HotelDto hotelDto) {
        // HotelDto is validated against constraints (not blank fields, valid contactInfo fields) before update
        HotelDto hotel = hotelService.updateHotelById(hotelId, hotelDto);
        return ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Delete a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId) {
        hotelService.deleteHotelById(hotelId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    @Operation(summary = "Activate a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> activateHotel(@PathVariable Long hotelId) {
        hotelService.activateHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all hotels owned by admin", tags = {"Admin Hotel"})
    public ResponseEntity<Page<HotelDto>> getAllHotels(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(hotelService.getAllHotels(pageable));
    }

    @GetMapping("/{hotelId}/bookings")
    @Operation(summary = "Get all bookings of a hotel", tags = {"Admin Bookings"})
    public ResponseEntity<Page<BookingDto>> getAllBookingsByHotelId(
            @PathVariable Long hotelId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(bookingService.getAllBookingsByHotelId(hotelId, pageable));
    }

    @GetMapping("/{hotelId}/reports")
    @Operation(summary = "Generate a bookings report of a hotel", tags = {"Admin Bookings"})
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false) LocalDate startDate,
                                                         @RequestParam(required = false) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        return ResponseEntity.ok(bookingService.getHotelReport(hotelId, startDate, endDate));
    }

    @PostMapping("/{hotelId}/photos")
    @Operation(summary = "Upload a photo for a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<String> uploadHotelPhoto(@PathVariable Long hotelId,
                                                    @RequestParam("file") MultipartFile file) {
        String url = hotelService.uploadHotelPhoto(hotelId, file);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/bookings/refund-pending")
    @Operation(summary = "Get all refund pending bookings", tags = {"Admin Bookings"})
    public ResponseEntity<List<BookingDto>> getRefundPendingBookings() {
        List<BookingDto> bookings = bookingService.getRefundPendingBookings();
        return ResponseEntity.ok(bookings);
    }
}

