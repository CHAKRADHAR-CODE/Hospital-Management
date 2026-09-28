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
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<Feedback> byDoctor(@PathVariable Long doctorId) {
        return feedbackService.getByDoctor(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    public List<Feedback> byPatient(@PathVariable Long patientId) {
        return feedbackService.getByPatient(patientId);
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Requests.FeedbackCreate req) {
        if (req.doctor == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Doctor is required"));
        }
        Long doctorId = Ids.parse(req.doctor.id, "Doctor");
        Feedback created = feedbackService.create(doctorId, req.rating, req.comment);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted: " + created.getId()));
    }
}
