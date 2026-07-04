package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Booking;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.User;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPaymentSessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    Page<Booking> findByHotel(Hotel hotel, Pageable pageable);

    List<Booking> findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Booking> findByUser(User user);

    Page<Booking> findByUser(User user, Pageable pageable);

    // Find stale bookings created before cutoff date matching status list
    List<Booking> findByBookingStatusInAndCreatedAtBefore(List<BookingStatus> statuses, LocalDateTime cutoff);

    List<Booking> findByBookingStatus(BookingStatus bookingStatus);

    // Counts recent bookings for a hotel within a lookback window for AI pricing.
    long countByHotelAndBookingStatusInAndCreatedAtAfter(Hotel hotel, List<BookingStatus> statuses, LocalDateTime after);

    // Finds bookings that checked out before the specified date with a given status
    List<Booking> findByBookingStatusAndCheckOutDateBefore(BookingStatus status, LocalDate date);
}

