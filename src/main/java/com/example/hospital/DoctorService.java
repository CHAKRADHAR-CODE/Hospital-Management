package com.example.hospital;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final FeedbackRepository feedbackRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationReportRepository consultationReportRepository;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         DepartmentRepository departmentRepository,
                         FeedbackRepository feedbackRepository,
                         AppointmentRepository appointmentRepository,
                         PrescriptionRepository prescriptionRepository,
                         ConsultationReportRepository consultationReportRepository,
                         CurrentUserService currentUserService,
                         UserService userService) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.consultationReportRepository = consultationReportRepository;
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    public Map<String, Object> createDoctor(String username, String email, Long departmentId,
                                            Doctor details) {
        User user;
        try {
            user = userService.registerAccountGenerated(username, email, Roles.DOCTOR);
        } catch (RuntimeException e) {
            throw e;
        }
        try {
            Doctor doctor = new Doctor();
            doctor.setUserId(user.getId());
            doctor.setName(details.getName());
            doctor.setSpecialization(details.getSpecialization());
            doctor.setPhone(details.getPhone());
            doctor.setQualification(details.getQualification());
            doctor.setYearsOfExperience(details.getYearsOfExperience());
            doctor.setBio(details.getBio());
            doctor.setDepartmentId(departmentId);
            doctorRepository.save(doctor);
        } catch (RuntimeException e) {
            userRepository.deleteById(user.getId());
            throw e;
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("password", user.getRawPassword());
        resp.put("role", Roles.DOCTOR);
        return resp;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::enrich).toList();
    }

    public List<Doctor> searchByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name).stream().map(this::enrich).toList();
    }

    public List<Doctor> searchBySpecialization(String spec) {
        return doctorRepository.findBySpecializationContainingIgnoreCase(spec).stream().map(this::enrich).toList();
    }

    public Doctor getDoctor(Long id) {
        return enrich(doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found")));
    }

    public Doctor getMe() {
        return enrich(currentUserService.currentDoctor());
    }

    public Doctor updateAvailability(Long id, List<Availability> availability) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        doctor.setAvailability(availability);
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor updates) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (updates.getName() != null && !updates.getName().isBlank()) doctor.setName(updates.getName());
        if (updates.getSpecialization() != null) doctor.setSpecialization(updates.getSpecialization());
        if (updates.getPhone() != null) doctor.setPhone(updates.getPhone());
        if (updates.getQualification() != null) doctor.setQualification(updates.getQualification());
        if (updates.getYearsOfExperience() != null) doctor.setYearsOfExperience(updates.getYearsOfExperience());
        if (updates.getBio() != null) doctor.setBio(updates.getBio());
        if (updates.getDepartmentId() != null) doctor.setDepartmentId(updates.getDepartmentId());
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        if (appointmentRepository.existsByDoctorId(id)
                || prescriptionRepository.existsByDoctorId(id)
                || feedbackRepository.existsByDoctorId(id)
                || consultationReportRepository.existsByDoctorId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete doctor with existing appointments or medical records");
        }
        doctorRepository.deleteById(id);
        if (doctor.getUserId() != null) {
            userRepository.deleteById(doctor.getUserId());
        }
    }

    public int countDoctors() {
        return (int) doctorRepository.count();
    }

    private Doctor enrich(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        Department dept = doctor.getDepartmentId() != null
                ? departmentRepository.findById(doctor.getDepartmentId()).orElse(null) : null;
        doctor.setDepartment(dept);
        if (dept != null) {
            doctor.setDepartmentName(dept.getName());
        }
        if (doctor.getUserId() != null) {
            userRepository.findById(doctor.getUserId()).ifPresent(u -> {
                doctor.setUsername(u.getUsername());
                doctor.setEmail(u.getEmail());
            });
        }
        List<Feedback> feedbacks = feedbackRepository.findByDoctorIdOrderByDateDesc(doctor.getId());
        if (!feedbacks.isEmpty()) {
            double avg = feedbacks.stream().mapToInt(Feedback::getRating).average().orElse(0);
            doctor.setAverageRating(Math.round(avg * 10.0) / 10.0);
            doctor.setRatingCount(feedbacks.size());
        } else {
            doctor.setAverageRating(0.0);
            doctor.setRatingCount(0);
        }
        return doctor;
    }
}
