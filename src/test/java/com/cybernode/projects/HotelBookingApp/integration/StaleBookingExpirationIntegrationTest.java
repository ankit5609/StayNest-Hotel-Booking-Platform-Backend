package com.cybernode.projects.HotelBookingApp.integration;

import com.cybernode.projects.HotelBookingApp.entity.*;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.repository.*;
import com.cybernode.projects.HotelBookingApp.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class StaleBookingExpirationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("hotel_db")
            .withUsername("postgres")
            .withPassword("postgres123");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Room room;
    private User user;
    private Hotel hotel;
    private Inventory inventory;

    @BeforeEach
    public void setUp() {
        bookingRepository.deleteAll();
        inventoryRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("John Doe");
        user.setEmail("john@test.com");
        user.setPassword("password");
        user = userRepository.save(user);

        hotel = new Hotel();
        hotel.setName("Ocean Breeze");
        hotel.setCity("Mumbai");
        hotel.setOwner(user);
        hotel.setActive(true);
        hotel = hotelRepository.save(hotel);

        room = new Room();
        room.setType("Ocean View");
        room.setBasePrice(BigDecimal.valueOf(100.00));
        room.setTotalCount(5);
        room.setCapacity(2);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        // Pre-create matching inventory date
        inventory = Inventory.builder()
                .hotel(hotel)
                .room(room)
                .bookedCount(0)
                .reservedCount(1)
                .city("Mumbai")
                .date(LocalDate.now().plusDays(2))
                .price(BigDecimal.valueOf(100.00))
                .surgeFactor(BigDecimal.ONE)
                .totalCount(5)
                .closed(false)
                .build();
        inventory = inventoryRepository.save(inventory);
    }

    @Test
    public void testExpireStaleBookings() {
        Booking staleBooking = new Booking();
        staleBooking.setUser(user);
        staleBooking.setHotel(hotel);
        staleBooking.setRoom(room);
        staleBooking.setCheckInDate(LocalDate.now().plusDays(2));
        staleBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        staleBooking.setRoomsCount(1);
        staleBooking.setAmount(BigDecimal.valueOf(100.00));
        staleBooking.setBookingStatus(BookingStatus.RESERVED);
        staleBooking.setCreatedAt(LocalDateTime.now().minusMinutes(15)); // stale (10 min expiry)
        staleBooking = bookingRepository.save(staleBooking);

        jdbcTemplate.update("UPDATE booking SET created_at = ? WHERE id = ?",
                LocalDateTime.now().minusMinutes(15), staleBooking.getId());

        bookingService.expireStaleBookings();

        Booking updated = bookingRepository.findById(staleBooking.getId()).orElseThrow();
        assertEquals(BookingStatus.EXPIRED, updated.getBookingStatus());

        Inventory updatedInv = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(0, updatedInv.getReservedCount());
    }
}
