package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.HotelSearchRequest;
import com.cybernode.projects.HotelBookingApp.dto.NaturalLanguageSearchRequestDto;
import com.cybernode.projects.HotelBookingApp.dto.NaturalLanguageSearchResponseDto;
import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationalSearchService {

    private final ChatClient chatClient;
    private final InventoryService inventoryService;

    @Value("${search.nl.enabled}")
    private boolean enabled;

    private static final List<String> REQUIRED_FIELDS =
            List.of("city", "startDate", "endDate", "roomsCount");

    private String systemPrompt() {
        return """
            You convert a hotel search sentence into JSON matching this shape:
            {
              "city": string or null,
              "startDate": "YYYY-MM-DD" or null,
              "endDate": "YYYY-MM-DD" or null,
              "roomsCount": integer or null,
              "minPrice": number or null,
              "maxPrice": number or null,
              "minRating": number 0-5 or null,
              "sortBy": one of "PRICE_ASC", "PRICE_DESC", "RATING_DESC", or null
            }

            Today's date is %s.

            Rules:
              - If the user doesn't clearly state a field, return null for it. NEVER guess.
              - Resolve relative dates ("next weekend", "in 3 days") into real dates using today's date.
              - "cheap"/"budget" implies sortBy=PRICE_ASC, not a specific minPrice/maxPrice unless a number is given.
              - Reply with ONLY the JSON object, no markdown fences, no explanation.
            """.formatted(LocalDate.now());
    }

    public NaturalLanguageSearchResponseDto search(NaturalLanguageSearchRequestDto request) {
        String query = request.getQuery();
        log.info("ConversationalSearchService: search query received: '{}'", query);
        if (!enabled) {
            log.warn("ConversationalSearchService: search is disabled!");
            return NaturalLanguageSearchResponseDto.builder()
                    .missingFields(List.of("feature_disabled"))
                    .build();
        }

        HotelSearchRequest parsed;
        try {
            log.info("ConversationalSearchService: Sending request to OpenRouter/Spring AI...");
            parsed = chatClient.prompt()
                    .system(systemPrompt())
                    .user(query)
                    .call()
                    .entity(HotelSearchRequest.class);
            log.info("ConversationalSearchService: Received response from AI: {}", parsed);
        } catch (Exception e) {
            log.warn("ConversationalSearchService: search parse failed for query '{}': {}", query, e.getMessage(), e);
            return NaturalLanguageSearchResponseDto.builder()
                    .missingFields(REQUIRED_FIELDS)
                    .build();
        }

        // Merge logic: If LLM returned null/empty for a field, fallback to the existing search context sent by the frontend
        if (parsed.getCity() == null || parsed.getCity().isBlank()) {
            parsed.setCity(request.getCity());
        }
        if (parsed.getStartDate() == null) {
            parsed.setStartDate(request.getStartDate());
        }
        if (parsed.getEndDate() == null) {
            parsed.setEndDate(request.getEndDate());
        }
        if (parsed.getRoomsCount() == null) {
            parsed.setRoomsCount(request.getRoomsCount());
        }
        if (parsed.getMinPrice() == null) {
            parsed.setMinPrice(request.getMinPrice());
        }
        if (parsed.getMaxPrice() == null) {
            parsed.setMaxPrice(request.getMaxPrice());
        }
        if (parsed.getMinRating() == null) {
            parsed.setMinRating(request.getMinRating());
        }
        if (parsed.getSortBy() == null || parsed.getSortBy() == SortOption.PRICE_ASC) {
            if (request.getSortBy() != null) {
                parsed.setSortBy(request.getSortBy());
            }
        }
        if (parsed.getStartDate() != null && parsed.getEndDate() != null) {
            if (!parsed.getEndDate().isAfter(parsed.getStartDate())) {
                if (parsed.getEndDate().getMonthValue() < parsed.getStartDate().getMonthValue()) {
                    parsed.setEndDate(parsed.getEndDate().plusYears(1));
                } else {
                    parsed.setEndDate(parsed.getStartDate().plusDays(2));
                }
            }
        }

        if (parsed.getPage() == null) {
            parsed.setPage(0);
        }
        if (parsed.getSize() == null) {
            parsed.setSize(10);
        }

        List<String> missing = new ArrayList<>();
        if (parsed.getCity() == null || parsed.getCity().isBlank()) missing.add("city");
        if (parsed.getStartDate() == null) missing.add("startDate");
        if (parsed.getEndDate() == null) missing.add("endDate");
        if (parsed.getRoomsCount() == null) missing.add("roomsCount");

        if (!missing.isEmpty()) {
            return NaturalLanguageSearchResponseDto.builder()
                    .interpretedQuery(parsed)
                    .missingFields(missing)
                    .build();
        }

        if (parsed.getSortBy() == null) {
            parsed.setSortBy(SortOption.PRICE_ASC);
        }

        return NaturalLanguageSearchResponseDto.builder()
                .interpretedQuery(parsed)
                .missingFields(List.of())
                .results(inventoryService.searchHotels(parsed))
                .build();
    }
}
