package com.stocktrading.user.service;

import com.stocktrading.common.exception.BusinessException;
import com.stocktrading.common.security.JwtTokenProvider;
import com.stocktrading.user.dto.AuthResponse;
import com.stocktrading.user.dto.LoginRequest;
import com.stocktrading.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());

        User user = userService.getUserByEmailOrUsername(request.getUsernameOrEmail());

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid username/email or password");
        }

        // Check account status
        if (user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_INACTIVE", "Your account is " + user.getAccountStatus());
        }

        // Update last login
        userService.updateLastLogin(user.getId());

        // Generate token
        String token = jwtTokenProvider.generateToken(user.getId().toString());

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .user(userService.getUserDTOById(user.getId()))
                .build();
    }

    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
