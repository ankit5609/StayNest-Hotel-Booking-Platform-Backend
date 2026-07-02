package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.AiPricingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiPricingServiceTest {

    @Mock
    private ChatClient chatClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private AiPricingService aiPricingService;

    private AiPricingContext context;

    @BeforeEach
    public void setUp() {
        aiPricingService = new AiPricingService(chatClient, objectMapper);

        ReflectionTestUtils.setField(aiPricingService, "enabled", true);
        ReflectionTestUtils.setField(aiPricingService, "minMultiplier", 0.8);
        ReflectionTestUtils.setField(aiPricingService, "maxMultiplier", 1.3);

        context = AiPricingContext.builder()
                .basePrice(BigDecimal.valueOf(100))
                .currentAdjustedPrice(BigDecimal.valueOf(120))
                .occupancyRate(0.5)
                .daysUntilCheckIn(10L)
                .surgeFactor(BigDecimal.ONE)
                .weekend(false)
                .recentBookingVelocity(2L)
                .hotelAverageRating(4.5)
                .hotelReviewCount(10L)
                .build();
    }

    private void stubChatClientResponse(String responseContent) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(responseContent);
    }

    @Test
    public void testGetMultiplier_Success() {
        stubChatClientResponse("{\"multiplier\": 1.08}");

        BigDecimal multiplier = aiPricingService.getMultiplier(context);

        assertEquals(0, BigDecimal.valueOf(1.08).compareTo(multiplier));
    }

    @Test
    public void testGetMultiplier_MarkdownFencesCleaned() {
        stubChatClientResponse("```json\n{\n  \"multiplier\": 1.15\n}\n```");

        BigDecimal multiplier = aiPricingService.getMultiplier(context);

        assertEquals(0, BigDecimal.valueOf(1.15).compareTo(multiplier));
    }

    @Test
    public void testGetMultiplier_ClampMax() {
        stubChatClientResponse("{\"multiplier\": 1.95}");

        BigDecimal multiplier = aiPricingService.getMultiplier(context);

        assertEquals(0, BigDecimal.valueOf(1.30).compareTo(multiplier));
    }

    @Test
    public void testGetMultiplier_ClampMin() {
        stubChatClientResponse("{\"multiplier\": 0.35}");

        BigDecimal multiplier = aiPricingService.getMultiplier(context);

        assertEquals(0, BigDecimal.valueOf(0.80).compareTo(multiplier));
    }

    @Test
    public void testGetMultiplier_Disabled() {
        ReflectionTestUtils.setField(aiPricingService, "enabled", false);

        BigDecimal multiplier = aiPricingService.getMultiplier(context);

        assertEquals(BigDecimal.ONE, multiplier);
        verifyNoInteractions(chatClient);
    }

    @Test
    public void testGetMultiplier_FallbackOnError() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("API error"));

        BigDecimal multiplier = aiPricingService.getMultiplier(context);

        assertEquals(BigDecimal.ONE, multiplier);
    }
}
