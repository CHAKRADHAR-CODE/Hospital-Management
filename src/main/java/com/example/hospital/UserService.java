package com.example.hospital;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles authentication and user account management.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ReceptionistRepository receptionistRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       ReceptionistRepository receptionistRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.receptionistRepository = receptionistRepository;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Creates and persists a new user account with a BCrypt-hashed password.
     */
    public User registerAccount(String username, String email, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }
        User user = new User(username, email, passwordEncoder.encode(rawPassword), role);
        user.setRawPassword(rawPassword);
        return userRepository.save(user);
    }

    /**
     * Creates an account with an auto-generated password and returns the user
     * carrying the plaintext password in {@code rawPassword}.
     */
    public User registerAccountGenerated(String username, String email, String role) {
        String raw = PasswordGenerator.generate();
        User user = registerAccount(username, email, raw, role);
        user.setRawPassword(raw);
        return user;
    }

    /**
     * Authenticates credentials against the database and verifies the stored role
     * matches the role selected on the frontend. Returns details of the user.
     */
    public Map<String, Object> login(String usernameOrEmail, String rawPassword, String selectedRole) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, rawPassword));
        } catch (Exception e) {
            throw new InvalidOperationException("Invalid username or password");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidOperationException("User not found"));

        if (!user.getRole().equalsIgnoreCase(selectedRole)) {
            SecurityContextHolder.clearContext();
            throw new AccessDeniedException("You are not registered as " + selectedRole);
        }
        if (!user.isEnabled()) {
            SecurityContextHolder.clearContext();
            throw new AccessDeniedException("Your account is disabled");
        }

        return buildLoginResponse(user);
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidOperationException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidOperationException("New password cannot be empty");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidOperationException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private Map<String, Object> buildLoginResponse(User user) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("dashboard", user.getRole().toLowerCase());

        if (Roles.DOCTOR.equalsIgnoreCase(user.getRole())) {
            doctorRepository.findByUserId(user.getId())
                    .ifPresent(d -> response.put("doctorId", d.getId()));
        } else if (Roles.PATIENT.equalsIgnoreCase(user.getRole())) {
            patientRepository.findByUserId(user.getId())
                    .ifPresent(p -> response.put("patientId", p.getId()));
        } else if (Roles.RECEPTIONIST.equalsIgnoreCase(user.getRole())) {
            receptionistRepository.findByUserId(user.getId())
                    .ifPresent(r -> response.put("receptionistId", r.getId()));
        }
        return response;
    }
}
