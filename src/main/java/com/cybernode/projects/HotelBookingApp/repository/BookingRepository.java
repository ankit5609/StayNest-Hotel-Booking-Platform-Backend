package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
