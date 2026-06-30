package com.cybernode.projects.HotelBookingApp.repository;


import com.cybernode.projects.HotelBookingApp.dto.HotelPriceDto;
import com.cybernode.projects.HotelBookingApp.entity.Hotel;
import com.cybernode.projects.HotelBookingApp.entity.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice, Long> {

    // Used for RATING_DESC sort — Pageable carries Sort.by("hotel.averageRating").descending()
    @Query("""
            SELECT new com.cybernode.projects.HotelBookingApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
                AND (:minRating IS NULL OR i.hotel.averageRating >= :minRating)
            GROUP BY i.hotel
            HAVING (:minPrice IS NULL OR AVG(i.price) >= :minPrice)
                AND (:maxPrice IS NULL OR AVG(i.price) <= :maxPrice)
            """)
    Page<HotelPriceDto> findHotels(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minRating") Double minRating,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("""
            SELECT new com.cybernode.projects.HotelBookingApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
                AND (:minRating IS NULL OR i.hotel.averageRating >= :minRating)
            GROUP BY i.hotel
            HAVING (:minPrice IS NULL OR AVG(i.price) >= :minPrice)
                AND (:maxPrice IS NULL OR AVG(i.price) <= :maxPrice)
            ORDER BY AVG(i.price) ASC
            """)
    Page<HotelPriceDto> findHotelsOrderByPriceAsc(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minRating") Double minRating,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );

    @Query("""
            SELECT new com.cybernode.projects.HotelBookingApp.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
                AND (:minRating IS NULL OR i.hotel.averageRating >= :minRating)
            GROUP BY i.hotel
            HAVING (:minPrice IS NULL OR AVG(i.price) >= :minPrice)
                AND (:maxPrice IS NULL OR AVG(i.price) <= :maxPrice)
            ORDER BY AVG(i.price) DESC
            """)
    Page<HotelPriceDto> findHotelsOrderByPriceDesc(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minRating") Double minRating,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
