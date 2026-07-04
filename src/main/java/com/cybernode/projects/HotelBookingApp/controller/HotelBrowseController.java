package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.dto.*;
import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import com.cybernode.projects.HotelBookingApp.service.ConversationalSearchService;
import com.cybernode.projects.HotelBookingApp.service.HotelService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;
    private final ConversationalSearchService conversationalSearchService;

    @GetMapping("/search")
    @Operation(summary = "Search hotels", tags = {"Browse Hotels"})
    public ResponseEntity<Page<HotelPriceResponseDto>> searchHotels(
            @RequestParam String city,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Integer roomsCount,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "PRICE_ASC") SortOption sortBy) {

        HotelSearchRequest hotelSearchRequest = new HotelSearchRequest();
        hotelSearchRequest.setCity(city);
        hotelSearchRequest.setStartDate(startDate);
        hotelSearchRequest.setEndDate(endDate);
        hotelSearchRequest.setRoomsCount(roomsCount);
        hotelSearchRequest.setPage(page);
        hotelSearchRequest.setSize(size);
        hotelSearchRequest.setMinPrice(minPrice);
        hotelSearchRequest.setMaxPrice(maxPrice);
        hotelSearchRequest.setMinRating(minRating);
        hotelSearchRequest.setSortBy(sortBy);

        return ResponseEntity.ok(inventoryService.searchHotels(hotelSearchRequest));
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

    @PostMapping("/search/nl")
    @Operation(summary = "Search hotels using natural language", tags = {"Browse Hotels"})
    public ResponseEntity<NaturalLanguageSearchResponseDto> searchHotelsNaturalLanguage(
            @Valid @RequestBody NaturalLanguageSearchRequestDto request) {
        return ResponseEntity.ok(conversationalSearchService.search(request));
    }

    @GetMapping
    @Operation(summary = "Get all active hotels with pagination, optional city filter and sorting", tags = {"Browse Hotels"})
    public ResponseEntity<Page<HotelDto>> getActiveHotels(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "top_rated") String sort) {

        Sort sortOrder;
        if ("newest".equalsIgnoreCase(sort)) {
            sortOrder = Sort.by(Sort.Direction.DESC, "createdAt");
        } else {
            sortOrder = Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.DESC, "reviewCount"));
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return ResponseEntity.ok(hotelService.getActiveHotels(city, pageable));
    }
}