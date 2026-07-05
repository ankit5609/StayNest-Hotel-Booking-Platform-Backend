package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cybernode.projects.HotelBookingApp.dto.*;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.Inventory;
import com.cybernode.projects.HotelBookingApp.entity.Room;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.SortOption;
import com.cybernode.projects.HotelBookingApp.exception.ResourceNotFoundException;
import com.cybernode.projects.HotelBookingApp.repository.HotelMinPriceRepository;
import com.cybernode.projects.HotelBookingApp.repository.InventoryRepository;
import com.cybernode.projects.HotelBookingApp.repository.RoomRepository;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService{
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);

        List<LocalDate> existingDates = inventoryRepository.findExistingDatesByRoomIdAndDateRange(room.getId(), today, endDate);
        Set<LocalDate> existingDatesSet = new HashSet<>(existingDates);

        List<Inventory> newInventories = new ArrayList<>();
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            if (existingDatesSet.contains(today)) {
                continue;
            }
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            newInventories.add(inventory);
        }
        if (!newInventories.isEmpty()) {
            inventoryRepository.saveAll(newInventories);
        }
    }

    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    private static final int MAX_SEARCH_PAGE_SIZE = 100;

    @Override
    public Page<HotelPriceResponseDto> searchHotels(HotelSearchRequest req) {
        log.info("Searching hotels for {} city, from {} to {}, sortBy={}", req.getCity(), req.getStartDate(), req.getEndDate(), req.getSortBy());

        String searchCity = req.getCity();
        if (searchCity != null && searchCity.contains(",")) {
            searchCity = searchCity.split(",")[0].trim();
        }

        int pageSize = Math.min(req.getSize(), MAX_SEARCH_PAGE_SIZE);
        SortOption sortBy = req.getSortBy() != null ? req.getSortBy() : SortOption.PRICE_ASC;

        Page<HotelPriceDto> hotelPage;

        if (sortBy == SortOption.RATING_DESC) {
            Pageable pageable = PageRequest.of(req.getPage(), pageSize,
                    Sort.by(Sort.Direction.DESC, "hotel.averageRating"));
            hotelPage = hotelMinPriceRepository.findHotels(
                    searchCity, req.getStartDate(), req.getEndDate(),
                    req.getMinRating(), req.getMinPrice(), req.getMaxPrice(), pageable);
        } else if (sortBy == SortOption.PRICE_DESC) {
            Pageable pageable = PageRequest.of(req.getPage(), pageSize);
            hotelPage = hotelMinPriceRepository.findHotelsOrderByPriceDesc(
                    searchCity, req.getStartDate(), req.getEndDate(),
                    req.getMinRating(), req.getMinPrice(), req.getMaxPrice(), pageable);
        } else {
            Pageable pageable = PageRequest.of(req.getPage(), pageSize);
            hotelPage = hotelMinPriceRepository.findHotelsOrderByPriceAsc(
                    searchCity, req.getStartDate(), req.getEndDate(),
                    req.getMinRating(), req.getMinPrice(), req.getMaxPrice(), pageable);
        }

        return hotelPage.map(hotelPriceDto -> {
            HotelPriceResponseDto dto = modelMapper.map(hotelPriceDto.getHotel(), HotelPriceResponseDto.class);
            dto.setPrice(hotelPriceDto.getPrice());
            return dto;
        });
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting All inventory by room for room with id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((element) -> modelMapper.map(element,
                        InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating All inventory by room for room with id: {} between date range: {} - {}", roomId,
                updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(), updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor());
    }

    @Override
    @Transactional
    public void updateInventoryForRoomChange(Room room, boolean priceChanged, boolean countChanged) {
        LocalDate today = LocalDate.now();

        List<Inventory> futureInventory = inventoryRepository.findAndLockFutureInventory(room.getId(), today);

        if (futureInventory.isEmpty()) {
            log.info("No future inventory found for room with id: {}, skipping inventory sync", room.getId());
            return;
        }
        if (countChanged) {
            Integer newTotalCount = room.getTotalCount();

            int maxCommitted = futureInventory.stream()
                    .mapToInt(inv -> inv.getBookedCount() + inv.getReservedCount())
                    .max()
                    .orElse(0);

            if (newTotalCount < maxCommitted) {
                throw new IllegalStateException(
                        "Cannot reduce totalCount to " + newTotalCount +
                                " — an existing date has " + maxCommitted + " rooms already booked/reserved for room id: " + room.getId()
                );
            }

            inventoryRepository.updateTotalCountForFutureDates(room.getId(), newTotalCount, today);
        }

        if (priceChanged) {
            inventoryRepository.updatePriceForFutureDates(room.getId(), room.getBasePrice(), today);
        }
    }

    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
