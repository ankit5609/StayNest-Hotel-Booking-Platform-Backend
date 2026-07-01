package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.HotelSearchRequest;
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

    public NaturalLanguageSearchResponseDto search(String query) {
        if (!enabled) {
            return NaturalLanguageSearchResponseDto.builder()
                    .missingFields(List.of("feature_disabled"))
                    .build();
        }

        HotelSearchRequest parsed;
        try {
            parsed = chatClient.prompt()
                    .system(systemPrompt())
                    .user(query)
                    .call()
                    .entity(HotelSearchRequest.class);
        } catch (Exception e) {
            log.warn("Conversational search parse failed for query '{}': {}", query, e.getMessage());
            return NaturalLanguageSearchResponseDto.builder()
                    .missingFields(REQUIRED_FIELDS)
                    .build();
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
