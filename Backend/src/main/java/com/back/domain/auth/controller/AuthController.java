package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignupRequest;
import com.back.domain.auth.service.AuthService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import com.back.global.security.jwt.JwtProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication API")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public ResponseEntity<RsData<Void>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        authService.login(request.username(), request.password());
        AuthService.TokenPair tokens = authService.createTokens(request.username());
        addTokenCookies(response, tokens.accessToken(), tokens.refreshToken());

        return ResponseEntity.ok(
                RsData.of("200", "Login successful")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<RsData<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {

        authService.logout(userDetails.getUsername());
        deleteTokenCookies(response);

        return ResponseEntity.ok(
                RsData.of("200", "Logout successful")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RsData<Void>> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {

        String newAccessToken = authService.refreshAccessToken(refreshToken);
        addAccessTokenCookie(response, newAccessToken);

        return ResponseEntity.ok(
                RsData.of("200", "Token refreshed successfully")
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<RsData<Void>> signup(
            @Valid @RequestBody SignupRequest request) {

        authService.signup(request);

        return ResponseEntity.ok(
                RsData.of("201", "Registration completed successfully")
        );
    }

    // ===== Private Helper Methods =====

    private void addTokenCookies(HttpServletResponse response,
                                 String accessToken,
                                 String refreshToken) {
        addAccessTokenCookie(response, accessToken);
        addRefreshTokenCookie(response, refreshToken);
    }

    private void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtProperties.getAccessTokenCookieMaxAge());
        response.addCookie(cookie);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtProperties.getRefreshTokenCookieMaxAge());
        response.addCookie(cookie);
    }

    private void deleteTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
    }
}