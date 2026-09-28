package com.example.hospital;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserService currentUserService;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           PatientRepository patientRepository,
                           DoctorRepository doctorRepository,
                           CurrentUserService currentUserService) {
        this.feedbackRepository = feedbackRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentUserService = currentUserService;
    }

    public Feedback create(Long doctorId, Integer rating, String comment) {
        Patient patient = currentUserService.currentPatient();
        if (patient == null) {
            throw new AccessDeniedException("Only patients can submit feedback");
        }
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidOperationException("Rating must be between 1 and 5");
        }
        Feedback feedback = new Feedback();
        feedback.setPatientId(patient.getId());
        feedback.setDoctorId(doctorId);
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setDate(LocalDate.now());
        feedback = feedbackRepository.save(feedback);
        feedback.setPatient(patient);
        feedback.setDoctor(doctor);
        return feedback;
    }

    public List<Feedback> getByDoctor(Long doctorId) {
        Doctor current = currentUserService.currentDoctor();
        if (current != null && !current.getId().equals(doctorId)) {
            throw new AccessDeniedException("You can only view your own feedback");
        }
        return feedbackRepository.findByDoctorIdOrderByDateDesc(doctorId).stream()
                .map(this::enrich).toList();
    }

    public List<Feedback> getByPatient(Long patientId) {
        currentUserService.requirePatientAccess(patientId);
        return feedbackRepository.findByPatientIdOrderByDateDesc(patientId).stream()
                .map(this::enrich).toList();
    }

    public int count() {
        return (int) feedbackRepository.count();
    }

    private Feedback enrich(Feedback feedback) {
        feedback.setPatient(patientRepository.findById(feedback.getPatientId()).orElse(null));
        feedback.setDoctor(doctorRepository.findById(feedback.getDoctorId()).orElse(null));
        return feedback;
    }
}
