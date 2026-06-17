package com.cybernode.projects.HotelBookingApp.dto;

import com.cybernode.projects.HotelBookingApp.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusResponseDto {
    private BookingStatus bookingStatus;
}
