package com.cybernode.projects.HotelBookingApp.repository;

import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);

    @Modifying
    @Query("""
            UPDATE Hotel h
            SET h.averageRating = COALESCE((SELECT AVG(CAST(r.rating as double)) FROM Review r WHERE r.hotel.id = :hotelId), 0.0),
                h.reviewCount = COALESCE((SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId), 0L)
            WHERE h.id = :hotelId
            """)
    void recalculateRating(@Param("hotelId") Long hotelId);
}
