package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.SignupRequest;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.user.service.UserService;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    @Transactional
    public TokenPair createTokens(String username) {
        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshToken = jwtTokenProvider.createRefreshToken(username);

        updateRefreshToken(username, refreshToken);

        return new TokenPair(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "User not found"));

        user.destroyRefreshToken();
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        validateRefreshToken(refreshToken);

        String username = jwtTokenProvider.getUsername(refreshToken);
        verifyRefreshTokenOwnership(username, refreshToken);

        return jwtTokenProvider.createAccessToken(username);
    }

    @Transactional
    public void signup(SignupRequest request) {
        validateSignupRequest(request);

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .nickname(request.nickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    private void updateRefreshToken(String username, String refreshToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "User not found"));
        user.updateRefreshToken(refreshToken);
    }

    private void verifyRefreshTokenOwnership(String username, String refreshToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new ServiceException("403", "Refresh token does not match");
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new ServiceException("401", "Invalid Refresh Token");
        }
    }

    private void validateSignupRequest(SignupRequest request) {
        if (!request.isPasswordMatching()) {
            throw new ServiceException("400", "Passwords do not match");
        }

        if (userService.existsByUsername(request.username())) {
            throw new ServiceException("400", "Username already exists");
        }

        if (userService.existsByNickname(request.nickname())) {
            throw new ServiceException("400", "Nickname already exists");
        }

        if (userService.existsByEmail(request.email())) {
            throw new ServiceException("400", "Email already exists");
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}
