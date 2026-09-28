package com.example.hospital;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConsultationReportService {

    private final ConsultationReportRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserService currentUserService;

    public ConsultationReportService(ConsultationReportRepository consultationRepository,
                                     PatientRepository patientRepository,
                                     DoctorRepository doctorRepository,
                                     CurrentUserService currentUserService) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentUserService = currentUserService;
    }

    public ConsultationReport createForPatient(Long patientId, ConsultationReport updates) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = currentUserService.currentDoctor();
        if (doctor == null) {
            throw new AccessDeniedException("Only doctors can add consultation reports");
        }
        ConsultationReport report = new ConsultationReport();
        report.setPatientId(patient.getId());
        report.setDoctorId(doctor.getId());
        report.setDate(LocalDate.now());
        report.setDiagnosis(updates.getDiagnosis());
        report.setConsultationDetails(updates.getConsultationDetails());
        report.setTreatment(updates.getTreatment());
        report.setRecommendations(updates.getRecommendations());
        report = consultationRepository.save(report);
        report.setPatient(patient);
        report.setDoctor(doctor);
        return report;
    }

    public List<ConsultationReport> getByPatient(Long patientId) {
        currentUserService.requirePatientAccess(patientId);
        return consultationRepository.findByPatientIdOrderByDateDesc(patientId).stream()
                .map(this::enrichForPatient).toList();
    }

    public List<ConsultationReport> getByDoctor(Long doctorId) {
        Doctor current = currentUserService.currentDoctor();
        if (current != null && !current.getId().equals(doctorId)) {
            throw new AccessDeniedException("You can only view your own consultation reports");
        }
        return consultationRepository.findByDoctorIdOrderByDateDesc(doctorId).stream()
                .map(this::enrichForDoctor).toList();
    }

    public int count() {
        return (int) consultationRepository.count();
    }

    private ConsultationReport enrichForPatient(ConsultationReport report) {
        report.setDoctor(doctorRepository.findById(report.getDoctorId()).orElse(null));
        return report;
    }

    private ConsultationReport enrichForDoctor(ConsultationReport report) {
        report.setPatient(patientRepository.findById(report.getPatientId()).orElse(null));
        return report;
    }
}
