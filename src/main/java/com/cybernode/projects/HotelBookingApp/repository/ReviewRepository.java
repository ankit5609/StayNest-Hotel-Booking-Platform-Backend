package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBookingId(Long bookingId);
    Page<Review> findByHotelId(Long hotelId, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
}
