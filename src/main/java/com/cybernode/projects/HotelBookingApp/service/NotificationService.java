package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${mail.from.address}")
    private String fromAddress;

    public void sendBookingConfirmation(Booking booking) {
        String subject = "Your StayNest booking is confirmed!";
        String body = buildConfirmationBody(booking);
        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    public void sendBookingExpired(Booking booking) {
        String subject = "Your StayNest reservation has expired";
        String body = "Your hold on " + booking.getHotel().getName() + " expired before payment was completed. Feel free to book again.";
        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    public void sendPaymentFailed(Booking booking) {
        String subject = "Payment failed for your StayNest booking";
        String body = "We couldn't process your payment for " + booking.getHotel().getName() + ". Your reservation has been released — please try booking again.";
        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    public void sendBookingCancelled(Booking booking) {
        String subject = "Your StayNest booking is cancelled";
        String body = "Your booking at " + booking.getHotel().getName() + " has been successfully cancelled and your refund has been initiated.";
        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let email failure break the booking flow — log and move on
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildConfirmationBody(Booking booking) {
        return String.format("""
                Hi %s,

                Your booking is confirmed!

                Hotel: %s
                Room: %s
                Check-in: %s
                Check-out: %s
                Amount paid: ₹%s

                Booking ID: %d

                Thanks for choosing StayNest!
                """,
                booking.getUser().getName(), booking.getHotel().getName(), booking.getRoom().getType(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getAmount(), booking.getId());
    }
}
