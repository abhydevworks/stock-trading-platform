package com.stocktrading.user.service;

import com.stocktrading.common.exception.BusinessException;
import com.stocktrading.common.exception.ResourceNotFoundException;
import com.stocktrading.user.dto.SignUpRequest;
import com.stocktrading.user.dto.UserDTO;
import com.stocktrading.user.model.User;
import com.stocktrading.user.model.UserRole;
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
    public UserDTO registerUser(SignUpRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("USER_ALREADY_EXISTS", "User with email " + request.getEmail() + " already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("USERNAME_ALREADY_EXISTS", "Username " + request.getUsername() + " is already taken");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .kycStatus(User.KYCStatus.PENDING)
                .accountStatus(User.AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Add default USER role
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(UserRole.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        return convertToDTO(savedUser);
    }

    public User getUserByEmailOrUsername(String emailOrUsername) {
        return userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email or username: " + emailOrUsername));
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Transactional
    public User updateLastLogin(UUID userId) {
        User user = getUserById(userId);
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public UserDTO updateKYCStatus(UUID userId, User.KYCStatus status) {
        User user = getUserById(userId);
        user.setKycStatus(status);
        if (status == User.KYCStatus.APPROVED) {
            user.setKycVerifiedDate(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public UserDTO getUserDTOById(UUID userId) {
        return convertToDTO(getUserById(userId));
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .kycStatus(user.getKycStatus().toString())
                .accountStatus(user.getAccountStatus().toString())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
