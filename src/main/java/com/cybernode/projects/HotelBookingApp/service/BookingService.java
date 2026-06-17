package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.dto.GuestDto;
import com.stripe.model.Event;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);
}