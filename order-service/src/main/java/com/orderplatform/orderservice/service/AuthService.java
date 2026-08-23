package com.orderplatform.orderservice.service;

import com.orderplatform.orderservice.dto.AuthResponse;
import com.orderplatform.orderservice.dto.LoginRequest;
import com.orderplatform.orderservice.dto.RegisterRequest;
import com.orderplatform.orderservice.model.User;
import com.orderplatform.orderservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getEmail());

        return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getDisplayName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getDisplayName());
    }
}