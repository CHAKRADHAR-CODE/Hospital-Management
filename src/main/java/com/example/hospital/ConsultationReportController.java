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
@RequestMapping("/api/consultations")
public class ConsultationReportController {

    private final ConsultationReportService consultationService;

    public ConsultationReportController(ConsultationReportService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','RECEPTIONIST','ADMIN')")
    public List<ConsultationReport> byPatient(@PathVariable Long patientId) {
        return consultationService.getByPatient(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<ConsultationReport> byDoctor(@PathVariable Long doctorId) {
        return consultationService.getByDoctor(doctorId);
    }

    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> createForPatient(@PathVariable Long patientId,
                                                                @RequestBody ConsultationReport updates) {
        ConsultationReport created = consultationService.createForPatient(patientId, updates);
        return ResponseEntity.ok(ApiResponse.success("Consultation report added: " + created.getId()));
    }
}
