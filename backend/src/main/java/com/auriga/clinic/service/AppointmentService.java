package com.auriga.clinic.service;

import com.auriga.clinic.exception.ResourceNotFoundException;
import com.auriga.clinic.exception.SlotConflictException;
import com.auriga.clinic.model.*;
import com.auriga.clinic.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;

    private static final long LATE_CANCEL_THRESHOLD_HOURS = 2;
    private static final BigDecimal LATE_CANCEL_FEE = new BigDecimal("100.00");

    public AppointmentService(AppointmentRepository appointmentRepo,
                               DoctorRepository doctorRepo,
                               PatientRepository patientRepo) {
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    @Transactional
    public Appointment bookAppointment(Long doctorId, Long patientId,
                                        LocalDateTime start, LocalDateTime end) {

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + patientId));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .startTime(start)
                .endTime(end)
                .status(AppointmentStatus.BOOKED)
                .cancellationFee(BigDecimal.ZERO)
                .build();

        try {
            return appointmentRepo.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotConflictException(
                    "This doctor already has an appointment overlapping this time slot");
        }
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, LocalDateTime cancelledAt) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is already cancelled");
        }

        Duration noticeGiven = Duration.between(cancelledAt, appointment.getStartTime());
        boolean isLateCancel = noticeGiven.toHours() < LATE_CANCEL_THRESHOLD_HOURS;

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(cancelledAt);
        appointment.setCancellationFee(isLateCancel ? LATE_CANCEL_FEE : BigDecimal.ZERO);

        return appointmentRepo.save(appointment);
    }

    public List<Appointment> getDoctorSchedule(Long doctorId) {
        return appointmentRepo.findByDoctorIdAndStatusOrderByStartTimeAsc(
                doctorId, AppointmentStatus.BOOKED);
    }

    public List<Patient> searchPatientsByName(String name) {
        return patientRepo.findByNameContainingIgnoreCase(name);
    }

    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
}