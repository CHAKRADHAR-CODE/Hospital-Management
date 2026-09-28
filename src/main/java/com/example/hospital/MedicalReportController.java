package com.example.hospital;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class MedicalReportController {

    private final MedicalReportService reportService;
    private final CurrentUserService currentUserService;

    public MedicalReportController(MedicalReportService reportService,
                                   CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/patient/{patientId}")
    public List<MedicalReport> byPatient(@PathVariable Long patientId) {
        return reportService.getByPatient(patientId);
    }

    @PostMapping("/upload/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','RECEPTIONIST','ADMIN')")
    public ResponseEntity<Map<String, Object>> upload(@PathVariable Long patientId,
                                                      @RequestParam(required = false) String title,
                                                      @RequestParam(required = false) String reportType,
                                                      @RequestParam(required = false) String description,
                                                      @RequestParam(value = "file", required = false) MultipartFile file) {
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Title is required"));
        }
        MedicalReport saved = reportService.upload(patientId, title, reportType, description, file);
        return ResponseEntity.ok(ApiResponse.success("Report uploaded: " + saved.getId()));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        MedicalReport report = reportService.getReport(id);
        currentUserService.requirePatientAccess(report.getPatientId());
        String path = report.getFilePath();
        if (path == null || path.isBlank()) {
            throw new ResourceNotFoundException("No file attached to this report");
        }
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Report file not found");
        }
        Resource resource = new FileSystemResource(filePath);
        String filename = report.getFileName() != null ? report.getFileName() : "report";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
