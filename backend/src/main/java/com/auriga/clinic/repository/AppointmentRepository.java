package com.auriga.clinic.repository;

import com.auriga.clinic.model.Appointment;
import com.auriga.clinic.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Doctor ka pura din ka schedule, time ke hisaab se sorted
    List<Appointment> findByDoctorIdAndStatusOrderByStartTimeAsc(
            Long doctorId, AppointmentStatus status);

    // Patient ki saari appointments (by patient id, via join)
    List<Appointment> findByPatientId(Long patientId);
}