package com.auriga.clinic.controller;

import com.auriga.clinic.model.Doctor;
import com.auriga.clinic.repository.DoctorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepo;

    public DoctorController(DoctorRepository doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    @PostMapping
    public ResponseEntity<Doctor> create(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorRepo.save(doctor));
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> all() {
        return ResponseEntity.ok(doctorRepo.findAll());
    }
}