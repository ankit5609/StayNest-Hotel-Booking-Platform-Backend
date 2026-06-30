package com.cybernode.projects.HotelBookingApp.service;


import com.cybernode.projects.HotelBookingApp.dto.GuestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GuestService {

    Page<GuestDto> getAllGuests(Pageable pageable);

    void updateGuest(Long guestId, GuestDto guestDto);

    void deleteGuest(Long guestId);

    GuestDto addNewGuest(GuestDto guestDto);
}
