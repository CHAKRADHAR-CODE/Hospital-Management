package com.example.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDoctorIdOrderByDateDesc(Long doctorId);
    List<Feedback> findByPatientIdOrderByDateDesc(Long patientId);
    boolean existsByPatientId(Long patientId);
    boolean existsByDoctorId(Long doctorId);
    boolean existsByPatientIdAndDoctorIdAndAppointmentId(Long patientId, Long doctorId, Long appointmentId);
}
