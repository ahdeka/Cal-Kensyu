package com.back.domain.user.service;

import com.back.domain.auth.dto.response.UserInfoResponse;
import com.back.domain.user.dto.request.ChangePasswordRequest;
import com.back.domain.user.dto.request.UpdateProfileRequest;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfoResponse getUserInfo(String username) {
        User user = getUserByUsername(username);

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public UserInfoResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = getUserByUsername(username);

        validateNicknameNotDuplicate(user, request.nickname());

        validateEmailNotDuplicate(user, request.email());

        user.updateProfile(request.nickname(), request.email());

        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ServiceException("400", "Current password is incorrect");
        }

        if (!request.isPasswordMatching()) {
            throw new ServiceException("400", "New passwords do not match");
        }

        user.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteAccount(String username) {
        User user = getUserByUsername(username);

        user.delete();

        log.info("User account soft deleted - username: {}, deleteDate: {}",
                username, user.getDeleteDate());
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "User not found"));
    }

    public void validateOwnership(String ownerUsername, String requestUsername) {
        if (!ownerUsername.equals(requestUsername)) {
            throw new ServiceException("403", "You do not have permission to perform this operation");
        }
    }

    /**
     * Validate if the requesting user is the owner (User entity overload)
     */
    public void validateOwnership(User owner, String requestUsername) {
        validateOwnership(owner.getUsername(), requestUsername);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateNicknameNotDuplicate(User currentUser, String newNickname) {
        if (currentUser.getNickname().equals(newNickname)) {
            return;
        }

        if (existsByNickname(newNickname)) {
            throw new ServiceException("400", "Nickname already exists");
        }
    }

    private void validateEmailNotDuplicate(User currentUser, String newEmail) {
        if (currentUser.getEmail().equals(newEmail)) {
            return;
        }

        if (existsByEmail(newEmail)) {
            throw new ServiceException("400", "Email already exists");
        }
    }
}
