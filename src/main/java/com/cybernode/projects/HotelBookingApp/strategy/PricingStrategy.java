package com.cybernode.projects.HotelBookingApp.strategy;


import com.cybernode.projects.HotelBookingApp.entity.Inventory;

import java.math.BigDecimal;
public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
