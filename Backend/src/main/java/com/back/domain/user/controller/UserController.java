package com.back.domain.user.controller;

import com.back.domain.auth.dto.response.UserInfoResponse;
import com.back.domain.user.dto.request.ChangePasswordRequest;
import com.back.domain.user.dto.request.UpdateProfileRequest;
import com.back.domain.user.service.UserService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User Management API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<RsData<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserInfoResponse userInfo = userService.getUserInfo(userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "User information retrieved successfully", userInfo)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<RsData<UserInfoResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserInfoResponse updatedUser = userService.updateProfile(
                userDetails.getUsername(),
                request
        );

        return ResponseEntity.ok(
                RsData.of("200", "Profile updated successfully", updatedUser)
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<RsData<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                RsData.of("200", "Password changed successfully")
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<RsData<Void>> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteAccount(userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "Account deleted successfully")
        );
    }

}
