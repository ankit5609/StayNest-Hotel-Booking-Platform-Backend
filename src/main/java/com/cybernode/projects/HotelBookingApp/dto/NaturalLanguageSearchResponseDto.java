package com.cybernode.projects.HotelBookingApp.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class NaturalLanguageSearchResponseDto {
    private HotelSearchRequest interpretedQuery;
    private List<String> missingFields;
    private Page<HotelPriceResponseDto> results;
}
