package com.auriga.clinic.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class BookingRequest {
    @NotNull
    private Long doctorId;

    @NotNull
    private Long patientId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    // Getters/setters (Lombok bhi use kar sakte ho, yahan simple rakha)
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}