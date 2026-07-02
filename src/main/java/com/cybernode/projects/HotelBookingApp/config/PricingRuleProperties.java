package com.cybernode.projects.HotelBookingApp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "pricing.rules")
@Data
public class PricingRuleProperties {

    private double occupancyThreshold = 0.8;
    private double occupancyMultiplier = 1.2;
    private int urgencyWindowDays = 7;
    private double urgencyMultiplier = 1.15;
    private double holidayMultiplier = 1.25;
    private Set<LocalDate> holidayDates = Set.of();
}
