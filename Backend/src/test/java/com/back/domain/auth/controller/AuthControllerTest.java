package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignupRequest;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController E2E Test
 * - Full HTTP request/response cycle
 * - Cookie-based authentication
 * - Real endpoint testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController E2E Test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private Cookie performLogin(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("accessToken");
    }

    // ========== Signup Tests ==========

    @Test
    @DisplayName("POST /api/auth/signup - Signup success")
    void signup_Success() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "password123",
                "new@test.com",
                "newnick"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.msg").value("Registration completed successfully"));

        // Verify user is saved in database
        User savedUser = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("new@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("newnick");
    }

    @Test
    @DisplayName("POST /api/auth/signup - Password mismatch returns 400")
    void signup_PasswordMismatch_Returns400() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "newuser",
                "password123",
                "differentPassword",  // Mismatch
                "new@test.com",
                "newnick"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("Passwords do not match"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - Duplicate username returns 400")
    void signup_DuplicateUsername_Returns400() throws Exception {
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
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("Username already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/signup - Invalid request body returns 400")
    void signup_InvalidRequestBody_Returns400() throws Exception {
        // given - empty username
        SignupRequest request = new SignupRequest(
                "",  // Invalid
                "password123",
                "password123",
                "new@test.com",
                "newnick"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== Login Tests ==========

    @Test
    @DisplayName("POST /api/auth/login - Login success with cookies")
    void login_Success() throws Exception {
        // given
        createTestUser("testuser", "password123");

        LoginRequest request = new LoginRequest("testuser", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Login successful"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().secure("refreshToken", true));

        // Verify RefreshToken is saved in database
        User user = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(user.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/auth/login - Wrong password returns 401")
    void login_WrongPassword_Returns401() throws Exception {
        // given
        createTestUser("testuser", "correctPassword");

        LoginRequest request = new LoginRequest("testuser", "wrongPassword");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Non-existent user returns 401")
    void login_UserNotFound_Returns401() throws Exception {
        // given
        LoginRequest request = new LoginRequest("nonexistent", "password");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Logout Tests ==========

    @Test
    @DisplayName("POST /api/auth/logout - Logout success")
    void logout_Success() throws Exception {
        // given
        User user = createTestUser("testuser", "password");
        Cookie accessToken = performLogin("testuser", "password");

        User userBeforeLogout = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(userBeforeLogout.getRefreshToken()).isNotNull();

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Logout successful"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        // Verify RefreshToken is cleared in database
        User userAfterLogout = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(userAfterLogout.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("POST /api/auth/logout - Without authentication returns 401")
    void logout_WithoutAuth_Returns401() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Refresh Token Tests ==========

    @Test
    @DisplayName("POST /api/auth/refresh - Refresh token success")
    void refreshToken_Success() throws Exception {
        // given
        createTestUser("testuser", "password");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("testuser", "password"))))
                .andReturn();

        Cookie oldAccessToken = loginResult.getResponse().getCookie("accessToken");
        Cookie refreshToken = loginResult.getResponse().getCookie("refreshToken");

        // Wait 1 second to ensure different timestamp
        Thread.sleep(1000);

        // when & then
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Token refreshed successfully"))
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        Cookie newAccessToken = refreshResult.getResponse().getCookie("accessToken");
        assertThat(newAccessToken.getValue()).isNotEqualTo(oldAccessToken.getValue());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Invalid refresh token returns 401")
    void refreshToken_InvalidToken_Returns401() throws Exception {
        // given
        Cookie invalidToken = new Cookie("refreshToken", "invalid.jwt.token");

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(invalidToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Without refresh token returns 400")
    void refreshToken_WithoutToken_Returns400() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/refresh"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("refreshToken cookie is required"));
    }

    // ========== Get Current User Tests ==========

    @Test
    @DisplayName("GET /api/auth/me - Get current user info success")
    void getCurrentUser_Success() throws Exception {
        // given
        createTestUser("testuser", "password");
        Cookie accessToken = performLogin("testuser", "password");

        // when & then
        mockMvc.perform(get("/api/auth/me")
                        .cookie(accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("User info retrieved successfully"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("testuser@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("testusernick"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Without authentication returns 401")
    void getCurrentUser_WithoutAuth_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/me"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/auth/me - Invalid token returns 401")
    void getCurrentUser_InvalidToken_Returns401() throws Exception {
        // given
        Cookie invalidToken = new Cookie("accessToken", "invalid.jwt.token");

        // when & then
        mockMvc.perform(get("/api/auth/me")
                        .cookie(invalidToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Full Authentication Flow Test ==========

    @Test
    @DisplayName("Full authentication flow - Signup -> Login -> Get user info -> Refresh -> Logout")
    void fullAuthenticationFlow() throws Exception {
        // 1. Signup
        SignupRequest signupRequest = new SignupRequest(
                "flowuser",
                "password123",
                "password123",
                "flow@test.com",
                "flownick"
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201"));

        // 2. Login
        LoginRequest loginRequest = new LoginRequest("flowuser", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie accessToken = loginResult.getResponse().getCookie("accessToken");
        Cookie refreshToken = loginResult.getResponse().getCookie("refreshToken");

        // 3. Get user info
        mockMvc.perform(get("/api/auth/me")
                        .cookie(accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("flowuser"));

        // 4. Refresh token
        Thread.sleep(1000);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        Cookie newAccessToken = refreshResult.getResponse().getCookie("accessToken");

        // 5. Logout
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));

        // Verify RefreshToken is cleared
        User user = userRepository.findByUsername("flowuser").orElseThrow();
        assertThat(user.getRefreshToken()).isNull();
    }
}