package com.stocktrading.user.controller;

import com.stocktrading.common.dto.ApiResponse;
import com.stocktrading.user.dto.AuthResponse;
import com.stocktrading.user.dto.LoginRequest;
import com.stocktrading.user.dto.SignUpRequest;
import com.stocktrading.user.dto.UserDTO;
import com.stocktrading.user.service.AuthService;
import com.stocktrading.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDTO>> signup(@Valid @RequestBody SignUpRequest request) {
        log.info("Signup request for email: {}", request.getEmail());
        UserDTO userDTO = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for: {}", request.getUsernameOrEmail());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        boolean isValid = authService.validateToken(actualToken);
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}
