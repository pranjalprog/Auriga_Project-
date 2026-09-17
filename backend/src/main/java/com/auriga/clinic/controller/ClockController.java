package com.auriga.clinic.controller;

import com.auriga.clinic.dto.SetClockRequest;
import com.auriga.clinic.service.ClockService;
import com.auriga.clinic.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/clock")
public class ClockController {

    private final ClockService clockService;
    private final NotificationService notificationService;

    public ClockController(ClockService clockService,
                           NotificationService notificationService) {
        this.clockService = clockService;
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, LocalDateTime>> setClock(
            @RequestBody SetClockRequest req) {

        clockService.setTime(req.getTime());

        notificationService.sendTodaysReminders(
                clockService.now()
        );

        return ResponseEntity.ok(
                Map.of("currentTime", clockService.now())
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, LocalDateTime>> getClock() {
        return ResponseEntity.ok(
                Map.of("currentTime", clockService.now())
        );
    }
}