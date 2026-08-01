package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.HotelMinPrice;
import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.repository.BookingRepository;
import com.cybernode.projects.HotelBookingApp.repository.HotelMinPriceRepository;
import com.cybernode.projects.HotelBookingApp.repository.HotelRepository;
import com.cybernode.projects.HotelBookingApp.repository.InventoryRepository;
import com.cybernode.projects.HotelBookingApp.strategy.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingUpdateService {

    // Scheduler to update the inventory and HotelMinPrice tables every hour

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;
    private final BookingRepository bookingRepository;

    @Value("${pricing.ai.velocity-lookback-days:7}")
    private int velocityLookbackDays;

    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        log.info("Starting scheduled hotel pricing update job...");
        int page = 0;
        int batchSize = 20;

        while (true) {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if (hotelPage.isEmpty()) {
                break;
            }
            for (Hotel hotel : hotelPage.getContent()) {
                try {
                    updateHotelPrices(hotel);
                } catch (Exception e) {
                    log.error("Error updating prices for hotel ID {}: {}", hotel.getId(), e.getMessage());
                }
            }
            page++;
        }
        log.info("Completed scheduled hotel pricing update job.");
    }

    @Transactional
    public void updateHotelPrices(Hotel hotel) {
        log.debug("Updating hotel prices for hotel ID: {}", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(60);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate, endDate);
        if (inventoryList.isEmpty()) {
            return;
        }

        // Compute booking velocity once for this hotel
        long velocity = bookingRepository.countByHotelAndBookingStatusInAndCreatedAtAfter(
                hotel, List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED), LocalDateTime.now().minusDays(velocityLookbackDays));

        updateInventoryPrices(inventoryList, velocity);
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice, Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) -> {
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date)
                    .orElse(new HotelMinPrice(hotel, date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList, long velocity) {
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory, velocity);
            inventory.setPrice(dynamicPrice);
        });
        inventoryRepository.saveAll(inventoryList);
    }

}
