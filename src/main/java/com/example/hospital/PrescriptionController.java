package com.example.hospital;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','RECEPTIONIST','ADMIN')")
    public List<Prescription> byPatient(@PathVariable Long patientId) {
        return prescriptionService.getByPatient(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<Prescription> byDoctor(@PathVariable Long doctorId) {
        return prescriptionService.getByDoctor(doctorId);
    }

    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> createForPatient(@PathVariable Long patientId,
                                                                @RequestBody Prescription updates) {
        Prescription created = prescriptionService.createForPatient(patientId, updates);
        return ResponseEntity.ok(ApiResponse.success("Prescription added: " + created.getId()));
    }
}
