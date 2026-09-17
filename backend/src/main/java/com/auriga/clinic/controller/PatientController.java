package com.auriga.clinic.controller;

import com.auriga.clinic.model.Patient;
import com.auriga.clinic.repository.PatientRepository;
import com.auriga.clinic.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepo;
    private final AppointmentService appointmentService;

    public PatientController(PatientRepository patientRepo, AppointmentService appointmentService) {
        this.patientRepo = patientRepo;
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientRepo.save(patient));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(@RequestParam String name) {
        return ResponseEntity.ok(appointmentService.searchPatientsByName(name));
    }
}
