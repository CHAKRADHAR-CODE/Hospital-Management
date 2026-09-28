package com.example.hospital;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Helper to resolve the currently logged-in user and their related document.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ReceptionistRepository receptionistRepository;

    public CurrentUserService(UserRepository userRepository,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              ReceptionistRepository receptionistRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.receptionistRepository = receptionistRepository;
    }

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("Not authenticated");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    public String currentRole() {
        return currentUser().getRole();
    }

    public Patient currentPatient() {
        User user = currentUser();
        return patientRepository.findByUserId(user.getId()).orElse(null);
    }

    public Doctor currentDoctor() {
        User user = currentUser();
        return doctorRepository.findByUserId(user.getId()).orElse(null);
    }

    public Receptionist currentReceptionist() {
        User user = currentUser();
        return receptionistRepository.findByUserId(user.getId()).orElse(null);
    }

    /**
     * Verifies a Patient has permission to access the given patient record.
     * Non-patient roles are allowed (checked separately by method security).
     */
    public void requirePatientAccess(Long patientId) {
        if (!Roles.PATIENT.equals(currentRole())) {
            return;
        }
        Patient patient = currentPatient();
        if (patient == null || !patient.getId().equals(patientId)) {
            throw new AccessDeniedException("You can only access your own data");
        }
    }
}
