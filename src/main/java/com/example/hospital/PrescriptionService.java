package com.example.hospital;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUserService currentUserService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               CurrentUserService currentUserService) {
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentUserService = currentUserService;
    }

    public Prescription createForPatient(Long patientId, Prescription updates) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = currentUserService.currentDoctor();
        if (doctor == null) {
            throw new AccessDeniedException("Only doctors can add prescriptions");
        }
        Prescription rx = new Prescription();
        rx.setPatientId(patient.getId());
        rx.setDoctorId(doctor.getId());
        rx.setDate(LocalDate.now());
        rx.setMedicine(updates.getMedicine());
        rx.setDosage(updates.getDosage());
        rx.setFrequency(updates.getFrequency());
        rx.setDuration(updates.getDuration());
        rx.setInstructions(updates.getInstructions());
        rx = prescriptionRepository.save(rx);
        rx.setPatient(patient);
        rx.setDoctor(doctor);
        return rx;
    }

    public List<Prescription> getByPatient(Long patientId) {
        currentUserService.requirePatientAccess(patientId);
        return prescriptionRepository.findByPatientIdOrderByDateDesc(patientId).stream()
                .map(this::enrichForPatient).toList();
    }

    public List<Prescription> getByDoctor(Long doctorId) {
        Doctor current = currentUserService.currentDoctor();
        if (current != null && !current.getId().equals(doctorId)) {
            throw new AccessDeniedException("You can only view your own prescriptions");
        }
        return prescriptionRepository.findByDoctorIdOrderByDateDesc(doctorId).stream()
                .map(this::enrichForDoctor).toList();
    }

    public int count() {
        return (int) prescriptionRepository.count();
    }

    private Prescription enrichForPatient(Prescription rx) {
        rx.setDoctor(doctorRepository.findById(rx.getDoctorId()).orElse(null));
        return rx;
    }

    private Prescription enrichForDoctor(Prescription rx) {
        rx.setPatient(patientRepository.findById(rx.getPatientId()).orElse(null));
        return rx;
    }
}
