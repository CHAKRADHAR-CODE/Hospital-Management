package com.example.hospital;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','RECEPTIONIST','ADMIN','DOCTOR')")
    public List<Bill> byPatient(@PathVariable Long patientId) {
        return billService.getByPatient(patientId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Requests.BillCreate req) {
        if (req.patient == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Patient is required"));
        }
        Long patientId = Ids.parse(req.patient.id, "Patient");
        Long appointmentId = req.appointment != null
                ? Ids.parseNullable(req.appointment.id, "Appointment") : null;
        Bill created = billService.create(patientId, appointmentId, req.amount, req.description);
        return ResponseEntity.ok(ApiResponse.success("Bill generated: " + created.getId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id,
                                                            @RequestParam String status) {
        billService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Bill status updated"));
    }
}
