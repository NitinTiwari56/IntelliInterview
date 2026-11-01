package com.interviewiq.auth.service;

import com.interviewiq.auth.dto.AuthResponse;
import com.interviewiq.auth.dto.LoginRequest;
import com.interviewiq.auth.dto.RegisterRequest;
import com.interviewiq.auth.dto.UserResponse;
import com.interviewiq.auth.entity.Role;
import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.exception.EmailAlreadyExistsException;
import com.interviewiq.auth.exception.InvalidCredentialsException;
import com.interviewiq.auth.repository.UserRepository;
import com.interviewiq.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Constructor injection: Spring khud teeno objects de dega.
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalize(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.STUDENT);   // ADMIN ko DB mein manually set karenge, API se nahi.

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(jwtUtil.generateToken(user), UserResponse.from(user));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
