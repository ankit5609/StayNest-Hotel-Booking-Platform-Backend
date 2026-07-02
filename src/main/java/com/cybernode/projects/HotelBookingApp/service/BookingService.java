package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.dto.HotelReportDto;
import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import com.stripe.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<Long> guestIdList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    BookingStatus getBookingStatus(Long bookingId);

    Page<BookingDto> getAllBookingsByHotelId(Long hotelId, Pageable pageable);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    Page<BookingDto> getMyBookings(Pageable pageable);

    List<BookingDto> getRefundPendingBookings();

    BookingDto getBookingDetails(Long bookingId);
}