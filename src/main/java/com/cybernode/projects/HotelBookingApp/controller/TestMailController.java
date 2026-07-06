package com.cybernode.projects.HotelBookingApp.controller;

import com.cybernode.projects.HotelBookingApp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestMailController {

    private final NotificationService notificationService;

    @GetMapping("/test-email")
    public String testEmail(@RequestParam String to) {
        notificationService.sendPasswordResetEmail(to, "test-connection-token");
        return "Test email triggered. Check your logs/inbox.";
    }
}
