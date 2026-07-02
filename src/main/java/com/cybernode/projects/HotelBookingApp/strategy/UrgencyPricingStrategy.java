package com.cybernode.projects.HotelBookingApp.strategy;

import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class UrgencyPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    private final int urgencyWindowDays;
    private final double urgencyMultiplier;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

        LocalDate today = LocalDate.now();
        LocalDate stayDate = inventory.getDate();
        boolean withinUrgencyWindow = !stayDate.isBefore(today)
                && stayDate.isBefore(today.plusDays(urgencyWindowDays));

        if (withinUrgencyWindow) {
            price = price.multiply(BigDecimal.valueOf(urgencyMultiplier));
        }
        return price;
    }
}
