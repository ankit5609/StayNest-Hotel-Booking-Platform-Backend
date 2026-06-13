package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.HotelDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelInfoDto;
import com.cybernode.projects.HotelBookingApp.dto.HotelSearchRequest;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest) {

        Page<HotelDto> page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId) {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }

}
