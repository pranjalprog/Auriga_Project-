package com.auriga.clinic.dto;

import java.time.LocalDateTime;

public class SetClockRequest {
    private LocalDateTime time;

    public LocalDateTime getTime() { return time; }
    public void setTime(LocalDateTime time) { this.time = time; }
}