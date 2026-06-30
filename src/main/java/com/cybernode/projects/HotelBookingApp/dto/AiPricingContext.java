package com.cybernode.projects.HotelBookingApp.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Snapshot of market signals passed to the AI pricing strategy.
 * All fields are read-only; built once per inventory row.
 */
@Value
@Builder
public class AiPricingContext {

    /** The room's original base price before any rule adjustments. */
    BigDecimal basePrice;

    /** Price after all deterministic decorators (surge, occupancy, urgency, holiday). */
    BigDecimal currentAdjustedPrice;

    /** Fraction of total capacity already booked for this inventory date (0.0 - 1.0). */
    double occupancyRate;

    /** Calendar days between today and the inventory check-in date. */
    long daysUntilCheckIn;

    /** Surge factor set on the inventory row by the hotel manager. */
    BigDecimal surgeFactor;

    /** Whether the inventory date falls on a Saturday or Sunday. */
    boolean weekend;

    /**
     * Count of CONFIRMED bookings for this hotel within the recent lookback
     * window (pre-computed once per hotel by PricingUpdateService, not
     * re-queried per row to avoid N+1 during the hourly batch job).
     */
    long recentBookingVelocity;

    /** Denormalized average guest rating on the Hotel entity (null if no reviews yet). */
    Double hotelAverageRating;

    /** Denormalized review count on the Hotel entity. */
    Long hotelReviewCount;
}
