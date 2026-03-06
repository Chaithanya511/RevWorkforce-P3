package com.rev.user_service.service.impl;

import com.rev.user_service.dto.request.LoginRequest;
import com.rev.user_service.dto.response.LoginResponse;
import com.rev.user_service.entity.User;
import com.rev.user_service.exception.BadRequestException;
import com.rev.user_service.repository.UserRepository;
import com.rev.user_service.security.JwtUtil;
import com.rev.user_service.security.PasswordEncoderUtil;
import com.rev.user_service.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoderUtil passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           JwtUtil jwtUtil,
                           PasswordEncoderUtil passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmployeeIdOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        if (!user.getActive()) {
            throw new BadRequestException("User account is deactivated");
        }

        String token = jwtUtil.generateToken(user);
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}