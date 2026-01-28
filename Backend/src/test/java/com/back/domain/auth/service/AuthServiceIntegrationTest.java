package com.back.domain.auth.service;

import com.back.domain.auth.dto.request.SignupRequest;
import com.back.domain.auth.dto.response.UserInfoResponse;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AuthService Integration Test
 * - Real database operations
 * - Spring Context loaded
 * - Full dependency injection
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthService Integration Test")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createTestUser(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(username + "@test.com")
                .nickname(username + "nick")
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    // ========== Signup Tests ==========

    @Test
    @DisplayName("Signup - User is saved to database")
    void signup_SavesUserToDatabase() {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "password123",
                "new@test.com",
                "newnick"
        );

        // when
        authService.signup(request);

        // then
        User savedUser = userRepository.findByUsername("newuser")
                .orElseThrow(() -> new AssertionError("User should be saved"));

        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("new@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("newnick");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    @DisplayName("Signup - Password is encrypted with BCrypt")
    void signup_EncryptsPassword() {
        // given
        SignupRequest request = new SignupRequest(
                "testuser",
                "plainPassword",
                "plainPassword",
                "test@test.com",
                "testnick"
        );

        // when
        authService.signup(request);

        // then
        User savedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("plainPassword");
        assertThat(savedUser.getPassword()).startsWith("$2a$"); // BCrypt prefix
        assertTrue(passwordEncoder.matches("plainPassword", savedUser.getPassword()));
    }

    @Test
    @DisplayName("Signup - Duplicate username throws exception")
    void signup_DuplicateUsername_ThrowsException() {
        // given
        createTestUser("existing", "password");

        SignupRequest request = new SignupRequest(
                "existing",  // Duplicate
                "password123",
                "password123",
                "new@test.com",
                "newnick"
        );

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Signup - Duplicate email throws exception")
    void signup_DuplicateEmail_ThrowsException() {
        // given
        createTestUser("user1", "password");

        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "password123",
                "user1@test.com",  // Duplicate email
                "newnick"
        );

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Email already exists");
    }

    // ========== Login & Token Tests ==========

    @Test
    @DisplayName("Login - Successful authentication with correct password")
    void login_Success() {
        // given
        createTestUser("testuser", "correctPassword");

        // when & then
        assertThatCode(() -> authService.login("testuser", "correctPassword"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Login - Fails with wrong password")
    void login_WrongPassword_ThrowsException() {
        // given
        createTestUser("testuser", "correctPassword");

        // when & then
        assertThatThrownBy(() -> authService.login("testuser", "wrongPassword"))
                .isInstanceOf(Exception.class);  // BadCredentialsException or similar
    }

    @Test
    @DisplayName("CreateTokens - RefreshToken is saved to database")
    void createTokens_SavesRefreshTokenToDatabase() {
        // given
        User user = createTestUser("testuser", "password");
        assertThat(user.getRefreshToken()).isNull();

        // when
        AuthService.TokenPair tokens = authService.createTokens("testuser");

        // then
        assertThat(tokens.accessToken()).isNotNull();
        assertThat(tokens.refreshToken()).isNotNull();

        // Verify RefreshToken is saved in DB
        User updatedUser = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updatedUser.getRefreshToken()).isEqualTo(tokens.refreshToken());
    }

    @Test
    @DisplayName("CreateTokens - Generated tokens are valid JWT")
    void createTokens_GeneratesValidJWT() {
        // given
        createTestUser("testuser", "password");

        // when
        AuthService.TokenPair tokens = authService.createTokens("testuser");

        // then
        assertThat(jwtTokenProvider.validateToken(tokens.accessToken())).isTrue();
        assertThat(jwtTokenProvider.validateToken(tokens.refreshToken())).isTrue();
        assertThat(jwtTokenProvider.getUsername(tokens.accessToken())).isEqualTo("testuser");
        assertThat(jwtTokenProvider.getUsername(tokens.refreshToken())).isEqualTo("testuser");
    }

    // ========== Logout Tests ==========

    @Test
    @DisplayName("Logout - RefreshToken is set to null in database")
    void logout_ClearsRefreshTokenInDatabase() {
        // given
        User user = createTestUser("testuser", "password");
        AuthService.TokenPair tokens = authService.createTokens("testuser");

        User userWithToken = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(userWithToken.getRefreshToken()).isNotNull();

        // when
        authService.logout("testuser");

        // then
        User userAfterLogout = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(userAfterLogout.getRefreshToken()).isNull();
    }

    // ========== Refresh Token Tests ==========

    @Test
    @DisplayName("RefreshAccessToken - New access token is issued with valid refresh token")
    void refreshAccessToken_Success() throws InterruptedException {
        // given
        createTestUser("testuser", "password");
        AuthService.TokenPair tokens = authService.createTokens("testuser");
        String oldRefreshToken = tokens.refreshToken();

        // Wait 1 second to ensure different timestamp in JWT
        Thread.sleep(1000);

        // when
        String newAccessToken = authService.refreshAccessToken(oldRefreshToken);

        // then
        assertThat(newAccessToken).isNotNull();
        assertThat(newAccessToken).isNotEqualTo(tokens.accessToken()); // New token generated
        assertThat(jwtTokenProvider.validateToken(newAccessToken)).isTrue();
        assertThat(jwtTokenProvider.getUsername(newAccessToken)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("RefreshAccessToken - Fails when token doesn't match database")
    void refreshAccessToken_TokenMismatch_ThrowsException() {
        // given
        createTestUser("testuser", "password");
        AuthService.TokenPair tokens = authService.createTokens("testuser");

        // Manually change RefreshToken in DB
        User user = userRepository.findByUsername("testuser").orElseThrow();
        user.updateRefreshToken("different.refresh.token");
        userRepository.save(user);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(tokens.refreshToken()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Refresh token does not match");
    }

    @Test
    @DisplayName("RefreshAccessToken - Fails with invalid token")
    void refreshAccessToken_InvalidToken_ThrowsException() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(invalidToken))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Invalid Refresh Token");
    }

    // ========== Get User Info Tests ==========

    @Test
    @DisplayName("GetUserInfo - Returns correct user information from database")
    void getUserInfo_ReturnsCorrectData() {
        // given
        User user = createTestUser("testuser", "password");

        // when
        UserInfoResponse userInfo = authService.getUserInfo("testuser");

        // then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.id()).isEqualTo(user.getId());
        assertThat(userInfo.username()).isEqualTo("testuser");
        assertThat(userInfo.email()).isEqualTo("testuser@test.com");
        assertThat(userInfo.nickname()).isEqualTo("testusernick");
        assertThat(userInfo.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("GetUserInfo - Throws exception for non-existent user")
    void getUserInfo_UserNotFound_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> authService.getUserInfo("nonexistent"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User not found");
    }

    // ========== Full Flow Test ==========

    @Test
    @DisplayName("Full authentication flow - Signup -> Login -> Logout")
    void fullAuthenticationFlow() {
        // 1. Signup
        SignupRequest signupRequest = new SignupRequest(
                "flowuser",
                "password123",
                "password123",
                "flow@test.com",
                "flownick"
        );
        authService.signup(signupRequest);

        User signedUpUser = userRepository.findByUsername("flowuser").orElseThrow();
        assertThat(signedUpUser).isNotNull();

        // 2. Login & Token Creation
        authService.login("flowuser", "password123");
        AuthService.TokenPair tokens = authService.createTokens("flowuser");

        User userWithToken = userRepository.findByUsername("flowuser").orElseThrow();
        assertThat(userWithToken.getRefreshToken()).isNotNull();

        // 3. Get User Info
        UserInfoResponse userInfo = authService.getUserInfo("flowuser");
        assertThat(userInfo.username()).isEqualTo("flowuser");

        // 4. Refresh Token
        String newAccessToken = authService.refreshAccessToken(tokens.refreshToken());
        assertThat(newAccessToken).isNotNull();

        // 5. Logout
        authService.logout("flowuser");
        User loggedOutUser = userRepository.findByUsername("flowuser").orElseThrow();
        assertThat(loggedOutUser.getRefreshToken()).isNull();
    }
}