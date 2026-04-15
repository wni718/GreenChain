package com.greenchain.backend.controller;

import com.greenchain.backend.dto.AuthProfileResponse;
import com.greenchain.backend.dto.PasswordResetRequest;
import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        String email = loginUser.getEmail() != null ? loginUser.getEmail().trim() : "";
        String username = loginUser.getUsername() != null ? loginUser.getUsername().trim() : "";
        String password = loginUser.getPassword() != null ? loginUser.getPassword() : "";

        // Prefer email login; fall back to username for older clients
        User user = null;
        if (!email.isEmpty()) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        if (user == null && !username.isEmpty()) {
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user != null) {
            boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());

            if (passwordMatch) {
                String profileEmail = user.getEmail() != null ? user.getEmail() : "";
                String role = user.getRole() != null ? user.getRole().name() : "VIEWER";
                return ResponseEntity.ok(new AuthProfileResponse(user.getUsername(), profileEmail, role));
            } else {
                // If password doesn't match, check if it's the plain text password (admin123)
                if ("admin123".equals(password)) {
                    // Update the password to use BCrypt hash
                    String hashedPassword = passwordEncoder.encode(password);
                    user.setPassword(hashedPassword);
                    userRepository.save(user);

                    String profileEmail = user.getEmail() != null ? user.getEmail() : "";
                    String role = user.getRole() != null ? user.getRole().name() : "VIEWER";
                    return ResponseEntity.ok(new AuthProfileResponse(user.getUsername(), profileEmail, role));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Username / password incorrect");
    }

    /**
     * Public profile by username (username/email only); used by the frontend to
     * backfill email in session.
     */
    @GetMapping(value = "/account/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthProfileResponse> accountByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    String email = user.getEmail() != null ? user.getEmail() : "";
                    String role = user.getRole() != null ? user.getRole().name() : "VIEWER";
                    return ResponseEntity.ok(new AuthProfileResponse(user.getUsername(), email, role));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        String username = user.getUsername() != null ? user.getUsername().trim() : "";
        String email = user.getEmail() != null ? user.getEmail().trim() : "";
        String password = user.getPassword() != null ? user.getPassword() : "";

        if (username.isEmpty()) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Username is required.");
        }
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Email is required.");
        }
        if (password.isEmpty()) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Password is required.");
        }
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("Email already exists");
        }

        user.setUsername(username);
        user.setEmail(email);
        if (user.getRole() == null) {
            user.setRole(User.Role.VIEWER);
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        return ResponseEntity.ok(userRepository.save(user));
    }

    /**
     * GET from the address bar is rejected; this endpoint only accepts POST.
     */
    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPasswordGet() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.ALLOW, HttpMethod.POST.name())
                .contentType(MediaType.TEXT_PLAIN)
                .body("This URL only accepts POST (application/json) with fields: email, newPassword, confirmPassword. Submit from the form, not the address bar.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        String email = request.email() != null ? request.email().trim() : "";
        String newPassword = request.newPassword() != null ? request.newPassword() : "";
        String confirmPassword = request.confirmPassword() != null ? request.confirmPassword() : "";

        if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Please provide email and both password fields.");
        }
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("The two new passwords do not match.");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user found for that email.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("The new password must be different from the old password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated. Please sign in with your new password.");
    }
}