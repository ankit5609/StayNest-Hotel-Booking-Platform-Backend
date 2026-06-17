package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.entity.Booking;

public interface CheckoutService {

    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);

}
