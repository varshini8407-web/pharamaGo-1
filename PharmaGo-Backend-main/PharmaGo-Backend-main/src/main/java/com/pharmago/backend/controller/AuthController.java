package com.pharmago.backend.controller;

import com.pharmago.backend.dto.JwtResponse;
import com.pharmago.backend.dto.LoginRequest;
import com.pharmago.backend.dto.RegisterRequest;
import com.pharmago.backend.entity.User;
import com.pharmago.backend.repository.UserRepository;
import com.pharmago.backend.security.JwtUtils;
import com.pharmago.backend.security.UserDetailsImpl;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        logger.info("LOGIN ENDPOINT REACHED");

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                JwtResponse.builder()
                        .token(jwt)
                        .id(userDetails.getId())
                        .username(userDetails.getUsername())
                        .email(userDetails.getEmail())
                        .roles(roles)
                        .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterRequest signUpRequest) {

        logger.info("REGISTER ENDPOINT REACHED");

        try {
            logger.info("Checking whether username already exists");

            if (userRepository.existsByUsername(
                    signUpRequest.getUsername())) {
                logger.warn("Registration rejected: username already exists");
                return ResponseEntity.badRequest()
                        .body("Error: Username is already taken!");
            }

            logger.info("Checking whether email already exists");

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Registration rejected: email already exists");
                return ResponseEntity.badRequest()
                        .body("Error: Email is already in use!");
            }

            // Pharmacist username rule enforcement
            if (signUpRequest.getRole() == User.Role.PHARMACIST &&
                    (signUpRequest.getUsername() == null || !signUpRequest.getUsername().toUpperCase().startsWith("ITA"))) {
                logger.warn("Registration rejected: Pharmacist username must start with ITA");
                return ResponseEntity.badRequest()
                        .body("Error: Pharmacist username must begin with 'ITA' prefix (e.g. ITA1024)!");
            }

            logger.info("Creating new user");

            User user = User.builder()
                    .username(signUpRequest.getUsername())
                    .email(signUpRequest.getEmail())
                    .password(encoder.encode(signUpRequest.getPassword()))
                    .fullName(signUpRequest.getFullName())
                    .phoneNumber(signUpRequest.getPhoneNumber())
                    .role(signUpRequest.getRole() != null
                            ? signUpRequest.getRole()
                            : User.Role.CUSTOMER)
                    .build();

            logger.info("Saving new user to database");

            userRepository.save(user);

            logger.info("Registration completed successfully");

            return ResponseEntity.ok("User registered successfully!");

        } catch (Exception e) {
            logger.error("Registration failed inside controller", e);

            return ResponseEntity.internalServerError()
                    .body("Registration failed. Check backend logs.");
        }
    }
}