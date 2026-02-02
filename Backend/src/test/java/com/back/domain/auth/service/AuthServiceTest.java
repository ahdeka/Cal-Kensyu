package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.SignupRequest;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.user.service.UserService;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService Unit Test
 * - Unit testing with Mockito
 * - Business logic validation
 * - External dependencies (DB, JWT) isolation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Test")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ========== Login Tests ==========

    @Test
    @DisplayName("Login success - AuthenticationManager is called properly")
    void login_Success() {
        // given
        String username = "testuser";
        String password = "password123";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Returns null on success (Authentication object is not important here)

        // when
        authService.login(username, password);

        // then
        verify(authenticationManager, times(1))
                .authenticate(argThat(token ->
                        token.getPrincipal().equals(username) &&
                                token.getCredentials().equals(password)
                ));
    }

    @Test
    @DisplayName("Login failure - Wrong password throws exception")
    void login_WrongPassword_ThrowsException() {
        // given
        String username = "testuser";
        String wrongPassword = "wrongpass";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        assertThatThrownBy(() -> authService.login(username, wrongPassword))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");
    }

    // ========== Token Creation Tests ==========

    @Test
    @DisplayName("Token creation success - AccessToken and RefreshToken are created properly")
    void createTokens_Success() {
        // given
        String username = "testuser";
        String accessToken = "access.token.jwt";
        String refreshToken = "refresh.token.jwt";

        User mockUser = User.builder()
                .username(username)
                .password("encodedPassword")
                .email("test@test.com")
                .nickname("testnick")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        when(jwtTokenProvider.createAccessToken(username)).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken(username)).thenReturn(refreshToken);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // when
        AuthService.TokenPair result = authService.createTokens(username);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        // Verify RefreshToken is updated in User entity (Dirty Checking will save in real environment)
        assertThat(mockUser.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Token creation failure - User not found")
    void createTokens_UserNotFound_ThrowsException() {
        // given
        String username = "nonexistent";

        when(jwtTokenProvider.createAccessToken(username)).thenReturn("access.token");
        when(jwtTokenProvider.createRefreshToken(username)).thenReturn("refresh.token");
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.createTokens(username))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User not found");
    }

    // ========== Logout Tests ==========

    @Test
    @DisplayName("Logout success - RefreshToken is updated to null")
    void logout_Success() {
        // given
        String username = "testuser";
        User mockUser = User.builder()
                .username(username)
                .password("password")
                .email("test@test.com")
                .nickname("testnick")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        mockUser.updateRefreshToken("existing.refresh.token");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // when
        authService.logout(username);

        // then
        assertThat(mockUser.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("Logout failure - User not found")
    void logout_UserNotFound_ThrowsException() {
        // given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.logout(username))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User not found");
    }

    // ========== Refresh Token Tests ==========

    @Test
    @DisplayName("Token refresh success - New AccessToken is issued")
    void refreshAccessToken_Success() {
        // given
        String username = "testuser";
        String oldRefreshToken = "old.refresh.token";
        String newAccessToken = "new.access.token";

        User mockUser = User.builder()
                .username(username)
                .password("password")
                .email("test@test.com")
                .nickname("testnick")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);
        mockUser.updateRefreshToken(oldRefreshToken);

        when(jwtTokenProvider.validateToken(oldRefreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(oldRefreshToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(jwtTokenProvider.createAccessToken(username)).thenReturn(newAccessToken);

        // when
        String result = authService.refreshAccessToken(oldRefreshToken);

        // then
        assertThat(result).isEqualTo(newAccessToken);
        verify(jwtTokenProvider, times(1)).validateToken(oldRefreshToken);
        verify(jwtTokenProvider, times(1)).getUsername(oldRefreshToken);
        verify(jwtTokenProvider, times(1)).createAccessToken(username);
    }

    @Test
    @DisplayName("Token refresh failure - Invalid RefreshToken")
    void refreshAccessToken_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(invalidToken))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Invalid Refresh Token");

        verify(jwtTokenProvider, never()).getUsername(anyString());
        verify(jwtTokenProvider, never()).createAccessToken(anyString());
    }

    @Test
    @DisplayName("Token refresh failure - RefreshToken does not match with DB")
    void refreshAccessToken_TokenMismatch_ThrowsException() {
        // given
        String username = "testuser";
        String requestToken = "request.refresh.token";
        String storedToken = "stored.refresh.token"; // Different token

        User mockUser = User.builder()
                .username(username)
                .password("password")
                .email("test@test.com")
                .nickname("testnick")
                .role(Role.USER)
                .build();
        mockUser.updateRefreshToken(storedToken);

        when(jwtTokenProvider.validateToken(requestToken)).thenReturn(true);
        when(jwtTokenProvider.getUsername(requestToken)).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(requestToken))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Refresh token does not match");

        verify(jwtTokenProvider, never()).createAccessToken(anyString());
    }

    // ========== Signup Tests ==========

    @Test
    @DisplayName("Signup success - User is saved properly")
    void signup_Success() {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "password123",
                "new@test.com",
                "newnick"
        );

        when(userService.existsByUsername(request.username())).thenReturn(false);
        when(userService.existsByNickname(request.nickname())).thenReturn(false);
        when(userService.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        // when
        authService.signup(request);

        // then
        verify(userRepository, times(1)).save(argThat(user ->
                user.getUsername().equals(request.username()) &&
                        user.getEmail().equals(request.email()) &&
                        user.getNickname().equals(request.nickname()) &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRole().equals(Role.USER)
        ));
    }

    @Test
    @DisplayName("Signup failure - Password mismatch")
    void signup_PasswordMismatch_ThrowsException() {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "differentPassword", // Mismatch
                "new@test.com",
                "newnick"
        );

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Passwords do not match");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Signup failure - Duplicate username")
    void signup_DuplicateUsername_ThrowsException() {
        // given
        SignupRequest request = new SignupRequest(
                "existinguser",
                "password123",
                "password123",
                "new@test.com",
                "newnick"
        );

        when(userService.existsByUsername(request.username())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Signup failure - Duplicate nickname")
    void signup_DuplicateNickname_ThrowsException() {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "password123",
                "new@test.com",
                "existingnick"
        );

        when(userService.existsByUsername(request.username())).thenReturn(false);
        when(userService.existsByNickname(request.nickname())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Nickname already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Signup failure - Duplicate email")
    void signup_DuplicateEmail_ThrowsException() {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "password123",
                "existing@test.com",
                "newnick"
        );

        when(userService.existsByUsername(request.username())).thenReturn(false);
        when(userService.existsByNickname(request.nickname())).thenReturn(false);
        when(userService.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }
}