package com.cybernode.projects.HotelBookingApp.strategy;

import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PricingStrategyTest {

    private Inventory inventory;
    private Room room;

    @BeforeEach
    public void setUp() {
        room = new Room();
        room.setBasePrice(BigDecimal.valueOf(100.00));
        room.setTotalCount(10);

        inventory = Inventory.builder()
                .room(room)
                .bookedCount(0)
                .reservedCount(0)
                .totalCount(10)
                .date(LocalDate.now().plusDays(10)) // default future date (beyond 7 days)
                .surgeFactor(BigDecimal.valueOf(1.5))
                .build();
    }

    @Test
    public void testBasePricingStrategy() {
        PricingStrategy base = new BasePricingStrategy();
        BigDecimal price = base.calculatePrice(inventory);
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(price));
    }

    @Test
    public void testSurgePricingStrategy() {
        PricingStrategy strategy = new SurgePricingStrategy(new BasePricingStrategy());
        BigDecimal price = strategy.calculatePrice(inventory);
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(price)); // 100 * 1.5
    }

    @Test
    public void testOccupancyPricingStrategy_HighOccupancy() {
        inventory.setBookedCount(9); // 9/10 = 90% > 80%
        PricingStrategy strategy = new OccupancyPricingStrategy(new BasePricingStrategy());
        BigDecimal price = strategy.calculatePrice(inventory);
        assertEquals(0, BigDecimal.valueOf(120.00).compareTo(price)); // 100 * 1.2
    }

    @Test
    public void testOccupancyPricingStrategy_LowOccupancy() {
        inventory.setBookedCount(5); // 5/10 = 50% <= 80%
        PricingStrategy strategy = new OccupancyPricingStrategy(new BasePricingStrategy());
        BigDecimal price = strategy.calculatePrice(inventory);
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(price)); // 100
    }

    @Test
    public void testUrgencyPricingStrategy_Urgent() {
        inventory.setDate(LocalDate.now().plusDays(3)); // < 7 days out
        PricingStrategy strategy = new UrgencyPricingStrategy(new BasePricingStrategy());
        BigDecimal price = strategy.calculatePrice(inventory);
        assertEquals(0, BigDecimal.valueOf(115.00).compareTo(price)); // 100 * 1.15
    }

    @Test
    public void testHolidayPricingStrategy() {
        PricingStrategy strategy = new HolidayPricingStrategy(new BasePricingStrategy());
        BigDecimal price = strategy.calculatePrice(inventory);
        assertEquals(0, BigDecimal.valueOf(125.00).compareTo(price)); // 100 * 1.25
    }
}
