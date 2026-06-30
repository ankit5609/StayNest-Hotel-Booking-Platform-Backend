package com.cybernode.projects.HotelBookingApp.strategy;

import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import com.cybernode.projects.HotelBookingApp.service.AiPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final AiPricingService aiPricingService;

    @Value("${pricing.ai.enabled}")
    private boolean aiEnabled;

    /**
     * Builds the deterministic decorator chain and, when AI pricing is enabled,
     * wraps it with the AI strategy as the outermost decorator.
     *
     * Velocity defaults to 0 in the no-argument overload (used for single booking
     * price checks at booking initiation time, where batch context is unavailable).
     * The AI sees 0 recent bookings, which will nudge the multiplier toward neutral.
     */
    public BigDecimal calculateDynamicPricing(Inventory inventory) {
        return calculateDynamicPricing(inventory, 0L);
    }

    /**
     * Full overload used by the hourly batch job (PricingUpdateService), which
     * pre-computes velocity once per hotel and passes it in here to avoid an
     * N+1 DB query across every inventory row of the same hotel.
     */
    public BigDecimal calculateDynamicPricing(Inventory inventory, long recentVelocity) {
        PricingStrategy pricingStrategy = new BasePricingStrategy();

        // Deterministic decorator chain (order matters -- each wraps the previous)
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);

        // AI decorator is outermost -- it sees the fully adjusted deterministic price.
        if (aiEnabled) {
            pricingStrategy = new AiDynamicPricingStrategy(pricingStrategy, aiPricingService, recentVelocity);
        }

        return pricingStrategy.calculatePrice(inventory);
    }

    //    Return the sum of price of this inventory list
    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList) {
        return inventoryList.stream()
                .map(this::calculateDynamicPricing)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
