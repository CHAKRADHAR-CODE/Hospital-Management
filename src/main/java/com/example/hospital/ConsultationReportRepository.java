package com.example.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationReportRepository extends JpaRepository<ConsultationReport, Long> {
    List<ConsultationReport> findByPatientIdOrderByDateDesc(Long patientId);
    List<ConsultationReport> findByDoctorIdOrderByDateDesc(Long doctorId);
    boolean existsByPatientId(Long patientId);
    boolean existsByDoctorId(Long doctorId);
}
