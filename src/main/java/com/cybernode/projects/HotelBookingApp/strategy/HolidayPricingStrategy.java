package com.cybernode.projects.HotelBookingApp.strategy;

import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy {

    private final PricingStrategy wrapped;
    private final Set<LocalDate> holidayDates;
    private final double holidayMultiplier;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

        boolean isStayDateHoliday = holidayDates.contains(inventory.getDate());
        if (isStayDateHoliday) {
            price = price.multiply(BigDecimal.valueOf(holidayMultiplier));
        }
        return price;
    }
}
