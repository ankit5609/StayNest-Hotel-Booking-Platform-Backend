package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.AiPricingContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calls the AI model (via OpenRouter) to obtain a price adjustment multiplier.
 *
 * Safety guarantees:
 *   - Returns BigDecimal.ONE (no adjustment) if the feature flag is disabled.
 *   - Strips markdown code fences from the response before parsing (Gemini Flash
 *     frequently wraps JSON in ```json ... ``` even when instructed not to).
 *   - Clamps the returned multiplier to [minMultiplier, maxMultiplier].
 *   - Catches all exceptions and returns BigDecimal.ONE on any failure, so a
 *     broken API key or network error never breaks the pricing pipeline.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiPricingService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Value("${pricing.ai.enabled}")
    private boolean enabled;

    @Value("${pricing.ai.min-multiplier}")
    private double minMultiplier;

    @Value("${pricing.ai.max-multiplier}")
    private double maxMultiplier;

    private static final String SYSTEM_PROMPT = """
            You are a hotel revenue management AI. Given a JSON snapshot of market signals,
            you must suggest a price adjustment multiplier for a single hotel room inventory date.
            Rules:
              - Reply ONLY with valid JSON in this exact format: {"multiplier": 1.08}
              - Do NOT wrap the JSON in markdown code fences or any other text.
              - The multiplier is applied on top of the price already adjusted by deterministic rules.
              - A multiplier of 1.0 means no change. Above 1.0 increases the price; below 1.0 decreases it.
              - Consider all provided signals holistically. High occupancy, low days-to-checkin,
                high booking velocity, and weekend dates generally support a higher multiplier.
                Low occupancy, far-future dates, and low velocity support a lower multiplier.
            """;

    /**
     * Returns a price multiplier for the given context.
     * Always safe to call -- falls back to 1.0x on any error.
     */
    public BigDecimal getMultiplier(AiPricingContext ctx) {
        if (!enabled) {
            return BigDecimal.ONE;
        }

        try {
            String userMessage = buildUserMessage(ctx);

            String raw = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();

            return parseAndClamp(raw);

        } catch (Exception e) {
            log.warn("AI pricing call failed, falling back to 1.0x multiplier. Reason: {}", e.getMessage());
            return BigDecimal.ONE;
        }
    }

    private String buildUserMessage(AiPricingContext ctx) {
        return String.format("""
                {
                  "basePrice": %s,
                  "currentAdjustedPrice": %s,
                  "occupancyRate": %.4f,
                  "daysUntilCheckIn": %d,
                  "surgeFactor": %s,
                  "isWeekend": %b,
                  "recentBookingVelocity": %d,
                  "hotelAverageRating": %s,
                  "hotelReviewCount": %d
                }
                """,
                ctx.getBasePrice(),
                ctx.getCurrentAdjustedPrice(),
                ctx.getOccupancyRate(),
                ctx.getDaysUntilCheckIn(),
                ctx.getSurgeFactor(),
                ctx.isWeekend(),
                ctx.getRecentBookingVelocity(),
                ctx.getHotelAverageRating() != null ? ctx.getHotelAverageRating() : "null",
                ctx.getHotelReviewCount() != null ? ctx.getHotelReviewCount() : 0
        );
    }

    /**
     * Strips markdown code fences that Gemini Flash tends to add, then parses the
     * multiplier and clamps it to [minMultiplier, maxMultiplier].
     */
    private BigDecimal parseAndClamp(String response) {
        try {
            // Strip ```json ... ``` or ``` ... ``` wrappers before parsing.
            String cleaned = response.replaceAll("(?s)```json\\s*|```", "").trim();

            JsonNode root = objectMapper.readTree(cleaned);
            double multiplier = root.path("multiplier").asDouble(1.0);

            // Clamp to safe bounds.
            double clamped = Math.max(minMultiplier, Math.min(maxMultiplier, multiplier));

            log.debug("AI pricing multiplier: raw={}, clamped={}", multiplier, clamped);
            return BigDecimal.valueOf(clamped).setScale(4, RoundingMode.HALF_UP);

        } catch (Exception e) {
            log.warn("Failed to parse AI multiplier from response: [{}]. Falling back to 1.0x.", response);
            return BigDecimal.ONE;
        }
    }
}
