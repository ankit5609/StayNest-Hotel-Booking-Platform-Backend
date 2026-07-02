package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.HotelSearchRequest;
import com.cybernode.projects.HotelBookingApp.dto.NaturalLanguageSearchResponseDto;
import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationalSearchServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ConversationalSearchService conversationalSearchService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(conversationalSearchService, "enabled", true);
    }

    private void stubChatClientEntityResponse(HotelSearchRequest response) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(HotelSearchRequest.class)).thenReturn(response);
    }

    @Test
    public void testSearch_Success() {
        HotelSearchRequest parsed = new HotelSearchRequest();
        parsed.setCity("Pune");
        parsed.setStartDate(LocalDate.now());
        parsed.setEndDate(LocalDate.now().plusDays(2));
        parsed.setRoomsCount(1);
        parsed.setSortBy(SortOption.PRICE_ASC);

        stubChatClientEntityResponse(parsed);

        NaturalLanguageSearchResponseDto response = conversationalSearchService.search("hotels in Pune");

        assertNotNull(response);
        assertTrue(response.getMissingFields().isEmpty());
        verify(inventoryService, times(1)).searchHotels(parsed);
    }

    @Test
    public void testSearch_MissingFields() {
        HotelSearchRequest parsed = new HotelSearchRequest();
        parsed.setCity(""); // missing city
        parsed.setStartDate(null); // missing startDate
        parsed.setEndDate(LocalDate.now().plusDays(2));
        parsed.setRoomsCount(1);

        stubChatClientEntityResponse(parsed);

        NaturalLanguageSearchResponseDto response = conversationalSearchService.search("hotels for next weekend");

        assertNotNull(response);
        assertFalse(response.getMissingFields().isEmpty());
        assertTrue(response.getMissingFields().contains("city"));
        assertTrue(response.getMissingFields().contains("startDate"));
        verifyNoInteractions(inventoryService);
    }

    @Test
    public void testSearch_Disabled() {
        ReflectionTestUtils.setField(conversationalSearchService, "enabled", false);

        NaturalLanguageSearchResponseDto response = conversationalSearchService.search("Pune");

        assertNotNull(response);
        assertEquals(List.of("feature_disabled"), response.getMissingFields());
        verifyNoInteractions(chatClient);
    }
}
