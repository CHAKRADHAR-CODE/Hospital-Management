package com.example.hospital;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientIdOrderByDateDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByDateDesc(Long doctorId);
    List<Appointment> findByStatusOrderByDateDesc(Appointment.Status status);
    List<Appointment> findAllByOrderByDateDesc();
    boolean existsByPatientId(Long patientId);
    boolean existsByDoctorId(Long doctorId);
    boolean existsByDoctorIdAndDateAndTime(Long doctorId, LocalDate date, LocalTime time);
    boolean existsByDoctorIdAndDateAndTimeAndIdNot(Long doctorId, LocalDate date, LocalTime time, Long id);
    Optional<Appointment> findByDoctorIdAndDateAndTime(Long doctorId, LocalDate date, LocalTime time);
}
