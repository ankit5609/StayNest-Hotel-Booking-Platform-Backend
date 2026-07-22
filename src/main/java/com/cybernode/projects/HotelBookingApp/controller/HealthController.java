package com.cybernode.projects.HotelBookingApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Lightweight health-check endpoint.
 * Used by UptimeRobot (or any monitoring tool) to ping the server
 * every 5 minutes so the Render free-tier instance never sleeps.
 *
 * URL: GET /api/v1/health
 * Auth: public (no JWT required)
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "StayNest Backend",
                "timestamp", Instant.now().toString()
        ));
    }
}
