package com.back.domain.vocabulary.controller;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.back.domain.vocabulary.dto.request.VocabularyUpdateRequest;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import com.back.domain.vocabulary.repository.VocabularyRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VocabularyController E2E Test
 * - Full HTTP request/response cycle
 * - Cookie-based authentication
 * - JSON parsing and validation
 * - Real database interactions
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("VocabularyController E2E Test")
class VocabularyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VocabularyRepository vocabularyRepository;

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
        testUser1 = createAndSaveUser("user1", "password1");
        testUser2 = createAndSaveUser("user2", "password2");

        user1AccessToken = performLogin("user1", "password1");
        user2AccessToken = performLogin("user2", "password2");
    }

    @AfterEach
    void tearDown() {
        vocabularyRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createAndSaveUser(String username, String password) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(username + "@test.com")
                .nickname(username + "Nick")
                .role(Role.USER)
                .build());
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

    private Vocabulary createVocabulary(User user, String word, StudyStatus status) {
        return vocabularyRepository.save(Vocabulary.builder()
                .user(user)
                .word(word)
                .hiragana("ひらがな")
                .meaning("의미")
                .exampleSentence("例文です")
                .exampleTranslation("예문입니다")
                .studyStatus(status)
                .build());
    }

    // ========== Create Vocabulary Tests ==========

    @Test
    @DisplayName("POST /api/vocabularies - Create vocabulary returns 201")
    void createVocabulary_Success_Returns201() throws Exception {
        // given
        VocabularyCreateRequest request = new VocabularyCreateRequest(
                "単語",
                "たんご",
                "단어",
                "これは単語です",
                "이것은 단어입니다"
        );

        // when & then
        mockMvc.perform(post("/api/vocabularies")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201"))
                .andExpect(jsonPath("$.msg").value("Vocabulary registered successfully"))
                .andExpect(jsonPath("$.data.word").value("単語"))
                .andExpect(jsonPath("$.data.hiragana").value("たんご"))
                .andExpect(jsonPath("$.data.meaning").value("단어"))
                .andExpect(jsonPath("$.data.studyStatus").value("NOT_STUDIED"));
    }

    @Test
    @DisplayName("POST /api/vocabularies - Without authentication returns 401")
    void createVocabulary_WithoutAuth_Returns401() throws Exception {
        // given
        VocabularyCreateRequest request = new VocabularyCreateRequest(
                "単語", "たんご", "단어", null, null
        );

        // when & then
        mockMvc.perform(post("/api/vocabularies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/vocabularies - Invalid request body returns 400")
    void createVocabulary_InvalidBody_Returns400() throws Exception {
        // given - Missing required field 'word'
        String invalidJson = "{\"hiragana\":\"たんご\",\"meaning\":\"단어\"}";

        // when & then
        mockMvc.perform(post("/api/vocabularies")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ========== Get My Vocabularies Tests ==========

    @Test
    @DisplayName("GET /api/vocabularies - Returns user's vocabulary list")
    void getMyVocabularies_ReturnsUserList() throws Exception {
        // given
        createVocabulary(testUser1, "単語1", StudyStatus.NOT_STUDIED);
        createVocabulary(testUser1, "単語2", StudyStatus.STUDYING);
        createVocabulary(testUser2, "単語3", StudyStatus.COMPLETED); // Other user's

        // when & then
        mockMvc.perform(get("/api/vocabularies")
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/vocabularies - Without authentication returns 401")
    void getMyVocabularies_WithoutAuth_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/vocabularies"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Vocabularies By Status Tests ==========

    @Test
    @DisplayName("GET /api/vocabularies/status/{status} - Returns filtered list")
    void getVocabulariesByStatus_ReturnsFilteredList() throws Exception {
        // given
        createVocabulary(testUser1, "単語1", StudyStatus.NOT_STUDIED);
        createVocabulary(testUser1, "単語2", StudyStatus.STUDYING);
        createVocabulary(testUser1, "単語3", StudyStatus.STUDYING);

        // when & then
        mockMvc.perform(get("/api/vocabularies/status/STUDYING")
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/vocabularies/status/{status} - Without authentication returns 401")
    void getVocabulariesByStatus_WithoutAuth_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/vocabularies/status/STUDYING"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Search Vocabularies Tests ==========

    @Test
    @DisplayName("GET /api/vocabularies/search?keyword={keyword} - Returns matching results")
    void searchVocabularies_ReturnsMatchingResults() throws Exception {
        // given
        createVocabulary(testUser1, "勉強", StudyStatus.NOT_STUDIED);
        createVocabulary(testUser1, "学習", StudyStatus.STUDYING);

        // when & then
        mockMvc.perform(get("/api/vocabularies/search")
                        .param("keyword", "勉強")
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].word").value("勉強"));
    }

    @Test
    @DisplayName("GET /api/vocabularies/search - Without authentication returns 401")
    void searchVocabularies_WithoutAuth_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/vocabularies/search")
                        .param("keyword", "test"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Get Vocabulary Tests ==========

    @Test
    @DisplayName("GET /api/vocabularies/{id} - Returns vocabulary details")
    void getVocabulary_Success_Returns200() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(get("/api/vocabularies/{id}", vocabulary.getId())
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.word").value("単語"))
                .andExpect(jsonPath("$.data.studyStatus").value("NOT_STUDIED"));
    }

    @Test
    @DisplayName("GET /api/vocabularies/{id} - Without authentication returns 401")
    void getVocabulary_WithoutAuth_Returns401() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(get("/api/vocabularies/{id}", vocabulary.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/vocabularies/{id} - Not owner returns 403")
    void getVocabulary_NotOwner_Returns403() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(get("/api/vocabularies/{id}", vocabulary.getId())
                        .cookie(user2AccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));
    }

    @Test
    @DisplayName("GET /api/vocabularies/{id} - Not found returns 404")
    void getVocabulary_NotFound_Returns404() throws Exception {
        // when & then
        mockMvc.perform(get("/api/vocabularies/{id}", 999L)
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    // ========== Update Vocabulary Tests ==========

    @Test
    @DisplayName("PUT /api/vocabularies/{id} - Update vocabulary returns 200")
    void updateVocabulary_Success_Returns200() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "古い単語", StudyStatus.NOT_STUDIED);

        VocabularyUpdateRequest request = new VocabularyUpdateRequest(
                "新しい単語",
                "あたらしいたんご",
                "새로운 단어",
                "新しい例文",
                "새로운 예문",
                StudyStatus.COMPLETED
        );

        // when & then
        mockMvc.perform(put("/api/vocabularies/{id}", vocabulary.getId())
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.word").value("新しい単語"))
                .andExpect(jsonPath("$.data.studyStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("PUT /api/vocabularies/{id} - Not owner returns 403")
    void updateVocabulary_NotOwner_Returns403() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        VocabularyUpdateRequest request = new VocabularyUpdateRequest(
                "変更", "へんこう", "변경", null, null, StudyStatus.STUDYING
        );

        // when & then
        mockMvc.perform(put("/api/vocabularies/{id}", vocabulary.getId())
                        .cookie(user2AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));
    }

    @Test
    @DisplayName("PUT /api/vocabularies/{id} - Without authentication returns 401")
    void updateVocabulary_WithoutAuth_Returns401() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        VocabularyUpdateRequest request = new VocabularyUpdateRequest(
                "変更", "へんこう", "변경", null, null, StudyStatus.STUDYING
        );

        // when & then
        mockMvc.perform(put("/api/vocabularies/{id}", vocabulary.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Delete Vocabulary Tests ==========

    @Test
    @DisplayName("DELETE /api/vocabularies/{id} - Delete vocabulary returns 200")
    void deleteVocabulary_Success_Returns200() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(delete("/api/vocabularies/{id}", vocabulary.getId())
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));

        // Verify deletion
        assertThat(vocabularyRepository.findById(vocabulary.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/vocabularies/{id} - Not owner returns 403")
    void deleteVocabulary_NotOwner_Returns403() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(delete("/api/vocabularies/{id}", vocabulary.getId())
                        .cookie(user2AccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));

        // Verify not deleted
        assertThat(vocabularyRepository.findById(vocabulary.getId())).isPresent();
    }

    @Test
    @DisplayName("DELETE /api/vocabularies/{id} - Without authentication returns 401")
    void deleteVocabulary_WithoutAuth_Returns401() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(delete("/api/vocabularies/{id}", vocabulary.getId()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/vocabularies/{id} - Not found returns 404")
    void deleteVocabulary_NotFound_Returns404() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/vocabularies/{id}", 999L)
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"));
    }

    // ========== Update Study Status Tests ==========

    @Test
    @DisplayName("PATCH /api/vocabularies/{id}/status - Update status returns 200")
    void updateStudyStatus_Success_Returns200() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(patch("/api/vocabularies/{id}/status", vocabulary.getId())
                        .param("studyStatus", "COMPLETED")
                        .cookie(user1AccessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.studyStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("PATCH /api/vocabularies/{id}/status - Not owner returns 403")
    void updateStudyStatus_NotOwner_Returns403() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(patch("/api/vocabularies/{id}/status", vocabulary.getId())
                        .param("studyStatus", "COMPLETED")
                        .cookie(user2AccessToken))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403"));
    }

    @Test
    @DisplayName("PATCH /api/vocabularies/{id}/status - Without authentication returns 401")
    void updateStudyStatus_WithoutAuth_Returns401() throws Exception {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        mockMvc.perform(patch("/api/vocabularies/{id}/status", vocabulary.getId())
                        .param("studyStatus", "COMPLETED"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ========== Full Flow Test ==========

    @Test
    @DisplayName("Full flow - Create -> Get -> Update -> UpdateStatus -> Delete")
    void fullFlow_AllOperations() throws Exception {
        // 1. Create
        VocabularyCreateRequest createRequest = new VocabularyCreateRequest(
                "単語",
                "たんご",
                "단어",
                "これは単語です",
                "이것은 단어입니다"
        );

        MvcResult createResult = mockMvc.perform(post("/api/vocabularies")
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long vocabularyId = objectMapper.readTree(createResponse)
                .path("data").path("id").asLong();

        // 2. Get
        mockMvc.perform(get("/api/vocabularies/{id}", vocabularyId)
                        .cookie(user1AccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.word").value("単語"));

        // 3. Update
        VocabularyUpdateRequest updateRequest = new VocabularyUpdateRequest(
                "更新された単語",
                "こうしんされたたんご",
                "업데이트된 단어",
                "新しい例文",
                "새로운 예문",
                StudyStatus.STUDYING
        );

        mockMvc.perform(put("/api/vocabularies/{id}", vocabularyId)
                        .cookie(user1AccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.word").value("更新された単語"))
                .andExpect(jsonPath("$.data.studyStatus").value("STUDYING"));

        // 4. Update Status
        mockMvc.perform(patch("/api/vocabularies/{id}/status", vocabularyId)
                        .param("studyStatus", "COMPLETED")
                        .cookie(user1AccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studyStatus").value("COMPLETED"));

        // 5. Delete
        mockMvc.perform(delete("/api/vocabularies/{id}", vocabularyId)
                        .cookie(user1AccessToken))
                .andExpect(status().isOk());

        // Verify deletion
        assertThat(vocabularyRepository.findById(vocabularyId)).isEmpty();
    }
}