
package com.auriga.clinic.controller;

import com.auriga.clinic.dto.BookingRequest;
import com.auriga.clinic.dto.RescheduleRequest;
import com.auriga.clinic.model.Appointment;
import com.auriga.clinic.service.AppointmentService;
import com.auriga.clinic.service.ClockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ClockService clockService;

    public AppointmentController(AppointmentService appointmentService,
                                 ClockService clockService) {
        this.appointmentService = appointmentService;
        this.clockService = clockService;
    }

    @PostMapping
    public ResponseEntity<Appointment> book(
            @Valid @RequestBody BookingRequest req) {

        Appointment appointment = appointmentService.bookAppointment(
                req.getDoctorId(),
                req.getPatientId(),
                req.getStartTime(),
                req.getEndTime()
        );

        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(
            @PathVariable Long id) {

        Appointment appointment = appointmentService.cancelAppointment(id);

        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequest req) {

        Appointment appointment = appointmentService.rescheduleAppointment(
                id,
                req.getNewStartTime(),
                req.getNewEndTime()
        );

        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<Appointment>> doctorSchedule(
            @PathVariable Long doctorId) {

        return ResponseEntity.ok(
                appointmentService.getDoctorSchedule(doctorId)
        );
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> patientAppointments(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentsForPatient(patientId)
        );
    }
}

