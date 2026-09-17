package com.auriga.clinic.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ClockService {

    // Starts at real system time; can be advanced/set via POST /clock
    private final AtomicReference<LocalDateTime> currentTime =
            new AtomicReference<>(LocalDateTime.now());

    public LocalDateTime now() {
        return currentTime.get();
    }

    public void setTime(LocalDateTime newTime) {
        currentTime.set(newTime);
    }

    public void advanceBy(java.time.Duration duration) {
        currentTime.updateAndGet(t -> t.plus(duration));
    }
}