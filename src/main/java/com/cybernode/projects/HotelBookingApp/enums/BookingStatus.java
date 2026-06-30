package com.cybernode.projects.HotelBookingApp.enums;

public enum BookingStatus {
    // Initial state: room inventory is held/reserved for checkout
    RESERVED,

    // Guest metadata has been added to the booking reservation
    GUESTS_ADDED,

    // Stripe checkout session has been generated and is awaiting payment completion
    PAYMENTS_PENDING,

    // Payment has been captured successfully and booking is locked in
    CONFIRMED,

    // User or host has explicitly cancelled the confirmed booking (triggers refund)
    CANCELLED,

    // Payment was declined or failed during the checkout session
    PAYMENT_FAILED,

    // Stripe checkout session or hold expired without a payment attempt
    EXPIRED,

    // Refund is pending Stripe response
    REFUND_PENDING
}
