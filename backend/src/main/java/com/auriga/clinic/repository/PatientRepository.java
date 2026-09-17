package com.auriga.clinic.repository;

import com.auriga.clinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    // Beginner-friendly name-based search (case-insensitive, partial match)
    List<Patient> findByNameContainingIgnoreCase(String name);
}