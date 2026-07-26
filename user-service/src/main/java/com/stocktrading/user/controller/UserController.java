package com.stocktrading.user.controller;

import com.stocktrading.common.dto.ApiResponse;
import com.stocktrading.user.dto.UserDTO;
import com.stocktrading.user.model.User;
import com.stocktrading.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable UUID userId) {
        log.info("Fetching user: {}", userId);
        UserDTO user = userService.getUserDTOById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{userId}/kyc-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateKYCStatus(
            @PathVariable UUID userId,
            @RequestParam User.KYCStatus status) {
        log.info("Updating KYC status for user {} to {}", userId, status);
        UserDTO user = userService.updateKYCStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success("KYC status updated", user));
    }
}
