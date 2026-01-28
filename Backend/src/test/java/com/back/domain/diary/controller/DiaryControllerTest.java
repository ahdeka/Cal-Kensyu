package com.back.domain.diary.controller;

import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DiaryController E2E Test
 * - Full HTTP request/response cycle
 * - Cookie-based authentication
 * - Real endpoint testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("DiaryController E2E Test")
class DiaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;
    private Cookie user1AccessToken;
    private Cookie user2AccessToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create test users
        testUser1 = createAndSaveUser("user1", "password");
        testUser2 = createAndSaveUser("user2", "password");

        // Get access tokens
        user1AccessToken = performLogin("user1", "password");
        user2AccessToken = performLogin("user2", "password");
    }

    @AfterEach
    void tearDown() {
        diaryRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createAndSaveUser(String username, String password) {
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
        String loginJson = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                username, password
        );

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("accessToken");
    }

    private Diary createDiary(User user, LocalDate diaryDate, String title, boolean isPublic) {
        Diary diary = Diary.builder()
                .user(user)
                .diaryDate(diaryDate)
                .title(title)
                .content("Content for " + title)
                .isPublic(isPublic)
                .build();
        return diaryRepository.save(diary);
    }

    // ========== Create Diary Tests ==========

    @Test
    @DisplayName("POST /api/diary - Create diary success")
    void createDiary_Success() throws Exception {
        // given
        DiaryCreateRequest request = new DiaryCreateRequest(
                LocalDate.now().minusDays(1),
                "My First Diary",
                "Today was a great day",
                true
        );

        // when & then
        mockMvc.perform(post("/api/diary")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.msg").value("Diary created successfully"))
                .andExpect(jsonPath("$.data.title").value("My First Diary"))
                .andExpect(jsonPath("$.data.content").value("Today was a great day"))
                .andExpect(jsonPath("$.data.username").value("user1"))
                .andExpect(jsonPath("$.data.isPublic").value(true));

        // Verify saved in database
        assertThat(diaryRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/diary - Without authentication returns 401")
    void createDiary_WithoutAuth_Returns401() throws Exception {
        // given
        DiaryCreateRequest request = new DiaryCreateRequest(
                LocalDate.now(),
                "Title",
                "Content",
                true
        );

        // when & then
        mockMvc.perform(post("/api/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/diary - Duplicate date returns 400")
    void createDiary_DuplicateDate_Returns400() throws Exception {
        // given
        LocalDate diaryDate = LocalDate.now().minusDays(1);
        createDiary(testUser1, diaryDate, "First", true);

        DiaryCreateRequest request = new DiaryCreateRequest(
                diaryDate,
                "Second",
                "Content",
                true
        );

        // when & then
        mockMvc.perform(post("/api/diary")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400"))
                .andExpect(jsonPath("$.msg").value("A diary entry for this date already exists"));
    }

    @Test
    @DisplayName("POST /api/diary - Future date returns 400")
    void createDiary_FutureDate_Returns400() throws Exception {
        // given
        DiaryCreateRequest request = new DiaryCreateRequest(
                LocalDate.now().plusDays(1),
                "Future Diary",
                "Content",
                true
        );

        // when & then
        mockMvc.perform(post("/api/diary")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("Cannot set a future date"));
    }

    @Test
    @DisplayName("POST /api/diary - Invalid request body returns 400")
    void createDiary_InvalidRequestBody_Returns400() throws Exception {
        // given - empty title
        DiaryCreateRequest request = new DiaryCreateRequest(
                LocalDate.now(),
                "",  // Invalid
                "Content",
                true
        );

        // when & then
        mockMvc.perform(post("/api/diary")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== Get Public Diaries Tests ==========

    @Test
    @DisplayName("GET /api/diary/public - Get public diaries")
    void getPublicDiaries_Success() throws Exception {
        // given
        createDiary(testUser1, LocalDate.now().minusDays(1), "Public 1", true);
        createDiary(testUser1, LocalDate.now().minusDays(2), "Private", false);
        createDiary(testUser2, LocalDate.now().minusDays(3), "Public 2", true);

        // when & then
        mockMvc.perform(get("/api/diary/public"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].title").value(org.hamcrest.Matchers.containsInAnyOrder("Public 1", "Public 2")));
    }

    @Test
    @DisplayName("GET /api/diary/public - No authentication required")
    void getPublicDiaries_NoAuthRequired() throws Exception {
        // given
        createDiary(testUser1, LocalDate.now(), "Public", true);

        // when & then
        mockMvc.perform(get("/api/diary/public"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ========== Get My Diaries Tests ==========

    @Test
    @DisplayName("GET /api/diary/my - Get my diaries")
    void getMyDiaries_Success() throws Exception {
        // given
        createDiary(testUser1, LocalDate.now().minusDays(1), "My Public", true);
        createDiary(testUser1, LocalDate.now().minusDays(2), "My Private", false);
        createDiary(testUser2, LocalDate.now().minusDays(3), "Other", true);

        // when & then
        mockMvc.perform(get("/api/diary/my")
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[*].title").value(org.hamcrest.Matchers.containsInAnyOrder("My Public", "My Private")));
    }

    @Test
    @DisplayName("GET /api/diary/my - Without authentication returns 401")
    void getMyDiaries_WithoutAuth_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/diary/my"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Diary Tests ==========

    @Test
    @DisplayName("GET /api/diary/{id} - Get public diary without auth")
    void getDiary_PublicDiary_WithoutAuth() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Public", true);

        // when & then
        mockMvc.perform(get("/api/diary/{id}", diary.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.title").value("Public"))
                .andExpect(jsonPath("$.data.isPublic").value(true));
    }

    @Test
    @DisplayName("GET /api/diary/{id} - Get private diary by owner")
    void getDiary_PrivateDiary_ByOwner() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Private", false);

        // when & then
        mockMvc.perform(get("/api/diary/{id}", diary.getId())
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Private"))
                .andExpect(jsonPath("$.data.isPublic").value(false));
    }

    @Test
    @DisplayName("GET /api/diary/{id} - Private diary without auth returns 401")
    void getDiary_PrivateDiary_WithoutAuth_Returns401() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Private", false);

        // when & then
        mockMvc.perform(get("/api/diary/{id}", diary.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401"));
    }

    @Test
    @DisplayName("GET /api/diary/{id} - Private diary by other user returns 403")
    void getDiary_PrivateDiary_ByOtherUser_Returns403() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Private", false);

        // when & then
        mockMvc.perform(get("/api/diary/{id}", diary.getId())
                        .cookie(user2AccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"))
                .andExpect(jsonPath("$.msg").value("You do not have permission to view this diary"));
    }

    @Test
    @DisplayName("GET /api/diary/{id} - Non-existent diary returns 404")
    void getDiary_NotFound_Returns404() throws Exception {
        // when & then
        mockMvc.perform(get("/api/diary/{id}", 999L)
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    // ========== Update Diary Tests ==========

    @Test
    @DisplayName("PUT /api/diary/{id} - Update diary success")
    void updateDiary_Success() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now().minusDays(2), "Original", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.now().minusDays(1),
                "Updated Title",
                "Updated Content",
                false
        );

        // when & then
        mockMvc.perform(put("/api/diary/{id}", diary.getId())
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Diary updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.content").value("Updated Content"))
                .andExpect(jsonPath("$.data.isPublic").value(false));

        // Verify in database
        Diary updated = diaryRepository.findById(diary.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("PUT /api/diary/{id} - Update by non-owner returns 403")
    void updateDiary_NotOwner_Returns403() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Diary", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.now(),
                "Hacked",
                "Content",
                true
        );

        // when & then
        mockMvc.perform(put("/api/diary/{id}", diary.getId())
                        .cookie(user2AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));

        // Verify diary unchanged
        Diary unchanged = diaryRepository.findById(diary.getId()).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Diary");
    }

    @Test
    @DisplayName("PUT /api/diary/{id} - Without authentication returns 401")
    void updateDiary_WithoutAuth_Returns401() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Diary", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.now(),
                "Updated",
                "Content",
                true
        );

        // when & then
        mockMvc.perform(put("/api/diary/{id}", diary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/diary/{id} - Date conflict returns 400")
    void updateDiary_DateConflict_Returns400() throws Exception {
        // given
        LocalDate conflictDate = LocalDate.now().minusDays(1);
        Diary diary1 = createDiary(testUser1, LocalDate.now().minusDays(2), "Diary 1", true);
        createDiary(testUser1, conflictDate, "Diary 2", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                conflictDate,
                "Updated",
                "Content",
                true
        );

        // when & then
        mockMvc.perform(put("/api/diary/{id}", diary1.getId())
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("A diary entry for this date already exists"));
    }

    // ========== Delete Diary Tests ==========

    @Test
    @DisplayName("DELETE /api/diary/{id} - Delete diary success")
    void deleteDiary_Success() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "To Delete", true);
        Long diaryId = diary.getId();

        // when & then
        mockMvc.perform(delete("/api/diary/{id}", diaryId)
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("Diary deleted successfully"));

        // Verify deleted from database
        assertThat(diaryRepository.findById(diaryId)).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/diary/{id} - Delete by non-owner returns 403")
    void deleteDiary_NotOwner_Returns403() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Diary", true);

        // when & then
        mockMvc.perform(delete("/api/diary/{id}", diary.getId())
                        .cookie(user2AccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));

        // Verify diary still exists
        assertThat(diaryRepository.findById(diary.getId())).isPresent();
    }

    @Test
    @DisplayName("DELETE /api/diary/{id} - Without authentication returns 401")
    void deleteDiary_WithoutAuth_Returns401() throws Exception {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Diary", true);

        // when & then
        mockMvc.perform(delete("/api/diary/{id}", diary.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/diary/{id} - Non-existent diary returns 404")
    void deleteDiary_NotFound_Returns404() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/diary/{id}", 999L)
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    // ========== Full Flow Test ==========

    @Test
    @DisplayName("Full flow - Create -> Get -> Update -> Delete")
    void fullFlow() throws Exception {
        // 1. Create
        DiaryCreateRequest createRequest = new DiaryCreateRequest(
                LocalDate.now().minusDays(1),
                "Flow Test",
                "Initial content",
                true
        );

        MvcResult createResult = mockMvc.perform(post("/api/diary")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long diaryId = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        // 2. Get
        mockMvc.perform(get("/api/diary/{id}", diaryId)
                        .cookie(user1AccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Flow Test"));

        // 3. Update
        DiaryUpdateRequest updateRequest = new DiaryUpdateRequest(
                LocalDate.now().minusDays(1),
                "Updated Flow",
                "Updated content",
                false
        );

        mockMvc.perform(put("/api/diary/{id}", diaryId)
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Flow"))
                .andExpect(jsonPath("$.data.isPublic").value(false));

        // 4. Delete
        mockMvc.perform(delete("/api/diary/{id}", diaryId)
                        .cookie(user1AccessToken))
                .andExpect(status().isOk());

        // Verify deleted
        assertThat(diaryRepository.findById(diaryId)).isEmpty();
    }
}