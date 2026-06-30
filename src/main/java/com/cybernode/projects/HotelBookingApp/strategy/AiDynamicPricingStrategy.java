package com.cybernode.projects.HotelBookingApp.strategy;

import com.cybernode.projects.HotelBookingApp.dto.AiPricingContext;
import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import com.cybernode.projects.HotelBookingApp.service.AiPricingService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Outermost decorator in the pricing chain. Consults the AI model for a
 * holistic price multiplier after all deterministic rules have run.
 *
 * Velocity is injected as a constructor parameter (pre-computed once per hotel
 * by PricingUpdateService) rather than queried inside calculatePrice, to avoid
 * an N+1 problem when the batch job iterates hundreds of inventory rows for
 * the same hotel.
 */
@RequiredArgsConstructor
public class AiDynamicPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    private final AiPricingService aiPricingService;

    /** Count of CONFIRMED bookings for this hotel in the lookback window. */
    private final long recentVelocity;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal deterministicPrice = wrapped.calculatePrice(inventory);

        LocalDate today = LocalDate.now();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, inventory.getDate());
        DayOfWeek dow = inventory.getDate().getDayOfWeek();
        boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;

        double occupancyRate = inventory.getTotalCount() > 0
                ? (double) inventory.getBookedCount() / inventory.getTotalCount()
                : 0.0;

        AiPricingContext context = AiPricingContext.builder()
                .basePrice(inventory.getRoom().getBasePrice())
                .currentAdjustedPrice(deterministicPrice)
                .occupancyRate(occupancyRate)
                .daysUntilCheckIn(daysUntilCheckIn)
                .surgeFactor(inventory.getSurgeFactor())
                .weekend(isWeekend)
                .recentBookingVelocity(recentVelocity)
                .hotelAverageRating(inventory.getHotel().getAverageRating())
                .hotelReviewCount(inventory.getHotel().getReviewCount())
                .build();

        BigDecimal multiplier = aiPricingService.getMultiplier(context);
        return deterministicPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
