package com.example.hospital;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    public static class LoginRequest {
        public String username;
        public String password;
        public String role;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (req.username == null || req.password == null || req.role == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            400,
                            "Username, password and role are required"));
        }

        String username = req.username.trim();
        String password = req.password;
        String role = req.role.trim();

        // Existing application-level login validation
        Map<String, Object> result =
                userService.login(username, password, role);

        // Authenticate with Spring Security
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                username,
                                password));

        // Create authenticated SecurityContext
        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Save authentication in HTTP session
        securityContextRepository.saveContext(
                context,
                request,
                response);

        return ResponseEntity.ok(result);
    }

    public static class ChangePasswordRequest {
        public String currentPassword;
        public String newPassword;
        public String confirmPassword;
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest req) {

        if (req.currentPassword == null || req.newPassword == null || req.confirmPassword == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400,
                            "Current password, new password and confirm password are required"));
        }

        userService.changePassword(
                req.currentPassword, req.newPassword, req.confirmPassword);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContextHolder.clearContext();

        request.getSession(false);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", 200);
        m.put("message", "Logged out successfully");

        return ResponseEntity.ok(m);
    }
}