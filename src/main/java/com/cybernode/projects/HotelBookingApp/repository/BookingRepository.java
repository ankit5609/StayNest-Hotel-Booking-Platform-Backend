package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Booking;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findByUser(User user);
}
