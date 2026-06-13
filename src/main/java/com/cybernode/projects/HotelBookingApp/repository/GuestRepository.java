package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}