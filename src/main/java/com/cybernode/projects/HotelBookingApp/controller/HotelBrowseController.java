package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.*;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    @Operation(summary = "Search hotels", tags = {"Browse Hotels"})
    public ResponseEntity<Page<HotelPriceResponseDto>> searchHotels(
            @RequestParam String city,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Integer roomsCount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        HotelSearchRequest hotelSearchRequest = new HotelSearchRequest();
        hotelSearchRequest.setCity(city);
        hotelSearchRequest.setStartDate(startDate);
        hotelSearchRequest.setEndDate(endDate);
        hotelSearchRequest.setRoomsCount(roomsCount);
        hotelSearchRequest.setPage(page);
        hotelSearchRequest.setSize(size);

        var pageResult = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/{hotelId}/info")
    @Operation(summary = "Get a hotel info by hotelId", tags = {"Browse Hotels"})
    public ResponseEntity<HotelInfoDto> getHotelInfo(
            @PathVariable Long hotelId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long roomsCount) {

        HotelInfoRequestDto hotelInfoRequestDto = new HotelInfoRequestDto();
        hotelInfoRequestDto.setStartDate(startDate);
        hotelInfoRequestDto.setEndDate(endDate);
        hotelInfoRequestDto.setRoomsCount(roomsCount);

        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId, hotelInfoRequestDto));
    }
}