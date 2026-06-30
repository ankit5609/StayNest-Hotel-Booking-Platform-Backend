package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Guest;
import com.cybernode.projects.HotelBookingApp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);

    Page<Guest> findByUser(User user, Pageable pageable);
}