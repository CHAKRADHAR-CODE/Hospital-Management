package com.example.hospital;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final ConsultationReportRepository consultationReportRepository;
    private final FeedbackRepository feedbackRepository;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    public PatientService(PatientRepository patientRepository,
                          UserRepository userRepository,
                          AppointmentRepository appointmentRepository,
                          BillRepository billRepository,
                          PrescriptionRepository prescriptionRepository,
                          MedicalReportRepository medicalReportRepository,
                          ConsultationReportRepository consultationReportRepository,
                          FeedbackRepository feedbackRepository,
                          CurrentUserService currentUserService,
                          UserService userService) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.consultationReportRepository = consultationReportRepository;
        this.feedbackRepository = feedbackRepository;
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    public Map<String, Object> createPatient(String username, String email, Patient details) {
        User user = userService.registerAccount(username, email, "Patient@123", Roles.PATIENT);
        try {
            Patient patient = new Patient();
            patient.setUserId(user.getId());
            patient.setName(details.getName());
            patient.setAge(details.getAge());
            patient.setGender(details.getGender());
            patient.setBloodGroup(details.getBloodGroup());
            patient.setPhone(details.getPhone());
            patient.setAddress(details.getAddress());
            patient.setRegistrationDate(LocalDate.now());
            patientRepository.save(patient);
        } catch (RuntimeException e) {
            userRepository.deleteById(user.getId());
            throw e;
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("password", user.getRawPassword());
        resp.put("role", Roles.PATIENT);
        return resp;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll().stream().map(this::enrich).toList();
    }

    public List<Patient> searchByName(String name) {
        return patientRepository.findByNameContainingIgnoreCase(name).stream().map(this::enrich).toList();
    }

    public Patient getPatient(Long id) {
        return enrich(patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found")));
    }

    public Patient getMe() {
        return enrich(currentUserService.currentPatient());
    }

    public Patient updatePatient(Long id, Patient updates) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (updates.getName() != null && !updates.getName().isBlank()) patient.setName(updates.getName());
        if (updates.getPhone() != null) patient.setPhone(updates.getPhone());
        if (updates.getAge() != null) patient.setAge(updates.getAge());
        if (updates.getGender() != null) patient.setGender(updates.getGender());
        if (updates.getBloodGroup() != null) patient.setBloodGroup(updates.getBloodGroup());
        if (updates.getAddress() != null) patient.setAddress(updates.getAddress());
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (appointmentRepository.existsByPatientId(id)
                || billRepository.existsByPatientId(id)
                || prescriptionRepository.existsByPatientId(id)
                || medicalReportRepository.existsByPatientId(id)
                || consultationReportRepository.existsByPatientId(id)
                || feedbackRepository.existsByPatientId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete patient with existing appointments, bills or medical records");
        }
        patientRepository.deleteById(id);
        if (patient.getUserId() != null) {
            userRepository.deleteById(patient.getUserId());
        }
    }

    public int countPatients() {
        return (int) patientRepository.count();
    }

    private Patient enrich(Patient patient) {
        if (patient == null) return null;
        if (patient.getUserId() != null) {
            userRepository.findById(patient.getUserId()).ifPresent(u -> patient.setEmail(u.getEmail()));
        }
        return patient;
    }
}
