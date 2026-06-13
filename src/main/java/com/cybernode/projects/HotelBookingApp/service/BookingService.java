package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.BookingDto;
import com.cybernode.projects.HotelBookingApp.dto.BookingRequest;
import com.cybernode.projects.HotelBookingApp.dto.GuestDto;

import java.util.List;

public interface BookingService {

    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
