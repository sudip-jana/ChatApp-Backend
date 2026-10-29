package com.sudip.chat.controller;

import com.sudip.chat.dto.LoginRequest;
import com.sudip.chat.dto.LoginResponse;
import com.sudip.chat.dto.RegisterRequest;
import com.sudip.chat.entity.AppUser;
import com.sudip.chat.repository.AppUserRepository;

import com.sudip.chat.security.JwtService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Transactional
    public Map<String, String> register(
            @Valid @RequestBody RegisterRequest request) {

        String username = request.username()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already taken");
        }

        AppUser user = new AppUser(
                username,
                passwordEncoder.encode(request.password()));

        try {
            userRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already taken");
        }

        return Map.of(
                "message", "Registration successful",
                "username", user.getUsername()
        );
    }


    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        String username = request.username()
                .toLowerCase(java.util.Locale.ROOT);

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid username or password"));

        if (!passwordEncoder.matches(
                request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new LoginResponse(
                token, "Bearer", 3600
        );
    }

}

