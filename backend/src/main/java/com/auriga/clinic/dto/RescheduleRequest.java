package com.auriga.clinic.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class RescheduleRequest {
    @NotNull
    private LocalDateTime newStartTime;

    @NotNull
    private LocalDateTime newEndTime;

    public LocalDateTime getNewStartTime() { return newStartTime; }
    public void setNewStartTime(LocalDateTime newStartTime) { this.newStartTime = newStartTime; }
    public LocalDateTime getNewEndTime() { return newEndTime; }
    public void setNewEndTime(LocalDateTime newEndTime) { this.newEndTime = newEndTime; }
}