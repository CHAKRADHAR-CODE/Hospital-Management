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
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final CurrentUserService currentUserService;

    public DoctorController(DoctorService doctorService, CurrentUserService currentUserService) {
        this.doctorService = doctorService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<Doctor> list(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String specialization) {
        if (name != null && !name.isBlank()) {
            return doctorService.searchByName(name);
        }
        if (specialization != null && !specialization.isBlank()) {
            return doctorService.searchBySpecialization(specialization);
        }
        return doctorService.getAllDoctors();
    }

    @GetMapping("/me")
    public Doctor me() {
        return doctorService.getMe();
    }

    @GetMapping("/{id}")
    public Doctor get(@PathVariable Long id) {
        return doctorService.getDoctor(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Requests.DoctorCreate req) {
        if (req.doctor == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Doctor details are required"));
        }
        Long departmentId = req.departmentId == null || req.departmentId.isBlank()
                ? null : parseId(req.departmentId, "Invalid department id");
        Map<String, Object> creds = doctorService.createDoctor(
                req.username, req.email, departmentId, req.doctor);
        return ResponseEntity.ok(creds);
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<Map<String, Object>> updateAvailability(@PathVariable Long id,
                                                                  @RequestBody List<Availability> availability) {
        String role = currentUserService.currentRole();
        Doctor current = currentUserService.currentDoctor();
        boolean isAdmin = Roles.ADMIN.equalsIgnoreCase(role);
        if (!isAdmin && (current == null || !current.getId().equals(id))) {
            throw new AccessDeniedException("You can only update your own availability");
        }
        doctorService.updateAvailability(id, availability);
        return ResponseEntity.ok(ApiResponse.success("Availability updated"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Doctor update(@PathVariable Long id, @RequestBody Doctor updates) {
        return doctorService.updateDoctor(id, updates);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor removed"));
    }

    private Long parseId(String value, String msg) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new InvalidOperationException(msg);
        }
    }
}
