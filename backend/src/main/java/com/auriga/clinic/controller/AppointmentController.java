package com.auriga.clinic.controller;

import com.auriga.clinic.dto.BookingRequest;
import com.auriga.clinic.dto.CancelRequest;
import com.auriga.clinic.model.Appointment;
import com.auriga.clinic.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Appointment> book(@Valid @RequestBody BookingRequest req) {
        Appointment appointment = appointmentService.bookAppointment(
                req.getDoctorId(), req.getPatientId(), req.getStartTime(), req.getEndTime());
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id,
                                               @RequestBody(required = false) CancelRequest req) {
        LocalDateTime cancelledAt = (req != null && req.getCancelledAt() != null)
                ? req.getCancelledAt()
                : LocalDateTime.now();
        Appointment appointment = appointmentService.cancelAppointment(id, cancelledAt);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<Appointment>> doctorSchedule(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorSchedule(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> patientAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(patientId));
    }
}