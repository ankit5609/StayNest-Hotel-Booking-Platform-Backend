package com.cybernode.projects.HotelBookingApp.strategy;

import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    private final double occupancyThreshold;
    private final double occupancyMultiplier;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

        if (inventory.getTotalCount() <= 0) {
            return price;
        }

        double occupancyRate = (double) inventory.getBookedCount() / inventory.getTotalCount();
        if (occupancyRate > occupancyThreshold) {
            price = price.multiply(BigDecimal.valueOf(occupancyMultiplier));
        }
        return price;
    }
}
