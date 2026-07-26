package com.stocktrading.user.service;

import com.stocktrading.common.exception.BusinessException;
import com.stocktrading.common.exception.ResourceNotFoundException;
import com.stocktrading.user.dto.SignupRequest;
import com.stocktrading.user.dto.UserDTO;
import com.stocktrading.user.model.User;
import com.stocktrading.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO signup(SignupRequest request) {
        log.info("Processing signup for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("USER_EXISTS", "User with this email already exists");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(User.UserStatus.ACTIVE)
                .emailVerified(false)
                .twoFactorEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getId());

        return convertToDTO(savedUser);
    }

    public UserDTO getUserProfile(UUID userId) {
        log.info("Fetching profile for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return convertToDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(UUID userId, UserDTO updates) {
        log.info("Updating profile for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (updates.getFirstName() != null) {
            user.setFirstName(updates.getFirstName());
        }
        if (updates.getLastName() != null) {
            user.setLastName(updates.getLastName());
        }
        if (updates.getPhone() != null) {
            user.setPhone(updates.getPhone());
        }
        if (updates.getAddress() != null) {
            user.setAddress(updates.getAddress());
        }
        if (updates.getCity() != null) {
            user.setCity(updates.getCity());
        }
        if (updates.getCountry() != null) {
            user.setCountry(updates.getCountry());
        }
        if (updates.getZipCode() != null) {
            user.setZipCode(updates.getZipCode());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);

        return convertToDTO(updatedUser);
    }

    @Transactional
    public void enableTwoFactor(UUID userId) {
        log.info("Enabling two-factor authentication for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setTwoFactorEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void disableTwoFactor(UUID userId) {
        log.info("Disabling two-factor authentication for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setTwoFactorEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .city(user.getCity())
                .country(user.getCountry())
                .zipCode(user.getZipCode())
                .status(user.getStatus().toString())
                .emailVerified(user.getEmailVerified())
                .twoFactorEnabled(user.getTwoFactorEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
