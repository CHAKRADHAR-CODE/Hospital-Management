package com.example.hospital;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final CurrentUserService currentUserService;

    public PatientController(PatientService patientService, CurrentUserService currentUserService) {
        this.patientService = patientService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public List<Patient> list(@RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return patientService.searchByName(name);
        }
        return patientService.getAllPatients();
    }

    @GetMapping("/me")
    public Patient me() {
        return patientService.getMe();
    }

    @GetMapping("/{id}")
    public Patient get(@PathVariable Long id) {
        currentUserService.requirePatientAccess(id);
        return patientService.getPatient(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Requests.PatientCreate req) {
        if (req.patient == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Patient details are required"));
        }
        return ResponseEntity.ok(patientService.createPatient(req.username, req.email, req.patient));
    }

    @PutMapping("/{id}")
    public Patient update(@PathVariable Long id, @RequestBody Patient updates) {
        String role = currentUserService.currentRole();
        if (Roles.PATIENT.equalsIgnoreCase(role)) {
            currentUserService.requirePatientAccess(id);
        } else if (!Roles.ADMIN.equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Access denied");
        }
        return patientService.updatePatient(id, updates);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient removed"));
    }
}
