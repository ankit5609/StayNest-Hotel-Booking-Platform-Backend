package com.cybernode.projects.HotelBookingApp.integration;

import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.entity.*;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.cybernode.projects.HotelBookingApp.enums.Role;
import com.cybernode.projects.HotelBookingApp.repository.*;
import com.cybernode.projects.HotelBookingApp.service.BookingService;
import com.cybernode.projects.HotelBookingApp.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class BookingFlowIntegrationTest {

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
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    private User user;
    private Hotel hotel;
    private Room room;

    @BeforeEach
    public void setUp() {
        inventoryRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("John Doe");
        user.setEmail("john@test.com");
        user.setPassword("password");
        user.setRoles(Set.of(Role.GUEST));
        user = userRepository.save(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );

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

        inventoryService.initializeRoomForAYear(room);
    }

    @Test
    public void testBookingIntegrationFlow() {
        BookingRequest request = new BookingRequest();
        request.setHotelId(hotel.getId());
        request.setRoomId(room.getId());
        request.setCheckInDate(LocalDate.now().plusDays(2));
        request.setCheckOutDate(LocalDate.now().plusDays(4));
        request.setRoomsCount(1);

        BookingDto bookingDto = bookingService.initialiseBooking(request);

        assertNotNull(bookingDto);
        assertEquals(BookingStatus.RESERVED, bookingDto.getBookingStatus());

        List<Inventory> inventories = inventoryRepository.findByHotelAndDateBetween(
                hotel, LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)
        );
        assertFalse(inventories.isEmpty());
        for (Inventory inv : inventories) {
            assertEquals(1, inv.getReservedCount());
        }
    }
}
