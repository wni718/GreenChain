package com.greenchain.backend.controller;

import com.greenchain.backend.model.User;
import com.greenchain.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername()).orElse(null);
        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return ResponseEntity.ok("Login successfully");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username / password incorrect");
    }
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // encrypted password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }
}