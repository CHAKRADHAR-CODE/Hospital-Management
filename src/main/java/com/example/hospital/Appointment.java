package com.example.hospital;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "appointments")
public class Appointment {

    public enum Status {
        SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient is required")
    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @NotNull(message = "Doctor is required")
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "Appointment time is required")
    @Column(nullable = false)
    private LocalTime time;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.SCHEDULED;

    @Column(columnDefinition = "TEXT")
    private String reason;

    public Appointment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Builds a JSON-friendly view with related patient and doctor information.
     */
    public Map<String, Object> toView(Patient patient, Doctor doctor) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", id);
        view.put("patientId", patientId);
        view.put("doctorId", doctorId);
        view.put("date", date);
        view.put("time", time);
        view.put("status", status);
        view.put("reason", reason);

        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", patient != null ? patient.getId() : patientId);
        p.put("name", patient != null ? patient.getName() : "Unknown");
        p.put("age", patient != null ? patient.getAge() : null);
        p.put("gender", patient != null ? patient.getGender() : null);
        p.put("phone", patient != null ? patient.getPhone() : null);
        p.put("bloodGroup", patient != null ? patient.getBloodGroup() : null);
        view.put("patient", p);

        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", doctor != null ? doctor.getId() : doctorId);
        d.put("name", doctor != null ? doctor.getName() : "Unknown");
        d.put("specialization", doctor != null ? doctor.getSpecialization() : "Unknown");
        d.put("phone", doctor != null ? doctor.getPhone() : null);
        view.put("doctor", d);

        return view;
    }
}
