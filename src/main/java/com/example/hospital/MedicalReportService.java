package com.example.hospital;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Service
public class MedicalReportService {

    private final MedicalReportRepository reportRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public MedicalReportService(MedicalReportRepository reportRepository,
                                PatientRepository patientRepository,
                                CurrentUserService currentUserService) {
        this.reportRepository = reportRepository;
        this.patientRepository = patientRepository;
        this.currentUserService = currentUserService;
    }

    public MedicalReport upload(Long patientId, String title, String reportType,
                                String description, MultipartFile file) {
        currentUserService.requirePatientAccess(patientId);
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        MedicalReport report = new MedicalReport();
        report.setPatientId(patientId);
        report.setTitle(title);
        report.setReportType(reportType);
        report.setDescription(description);
        report.setUploadedDate(LocalDate.now());

        if (file != null && !file.isEmpty()) {
            String safeName = sanitize(file.getOriginalFilename());
            String storedName = System.currentTimeMillis() + "_" + safeName;
            try {
                Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
                Files.createDirectories(dir);
                Path target = dir.resolve(storedName);
                file.transferTo(target);
                report.setFileName(safeName);
                report.setFilePath(target.toString());
                report.setFileSize(file.getSize());
            } catch (IOException e) {
                throw new InvalidOperationException("Failed to save report file");
            }
        }
        return reportRepository.save(report);
    }

    public List<MedicalReport> getByPatient(Long patientId) {
        currentUserService.requirePatientAccess(patientId);
        return reportRepository.findByPatientIdOrderByUploadedDateDesc(patientId);
    }

    public MedicalReport getReport(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }

    public String reportFilePath(Long id) {
        MedicalReport report = getReport(id);
        return report.getFilePath();
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) return "report";
        String cleaned = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isEmpty() ? "report" : cleaned;
    }
}
