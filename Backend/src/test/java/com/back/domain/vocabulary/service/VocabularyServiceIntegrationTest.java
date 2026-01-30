package com.back.domain.vocabulary.service;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.back.domain.vocabulary.dto.request.VocabularyUpdateRequest;
import com.back.domain.vocabulary.dto.response.VocabularyListResponse;
import com.back.domain.vocabulary.dto.response.VocabularyResponse;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import com.back.domain.vocabulary.repository.VocabularyRepository;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * VocabularyService Integration Test
 * - Real H2 database
 * - Real Spring beans
 * - Transaction management
 * - Database state verification
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("VocabularyService Integration Test")
class VocabularyServiceIntegrationTest {

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = userRepository.save(User.builder()
                .username("testuser1")
                .password("password1")
                .email("test1@test.com")
                .nickname("TestNick1")
                .role(Role.USER)
                .build());

        testUser2 = userRepository.save(User.builder()
                .username("testuser2")
                .password("password2")
                .email("test2@test.com")
                .nickname("TestNick2")
                .role(Role.USER)
                .build());
    }

    @AfterEach
    void tearDown() {
        vocabularyRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Create Vocabulary Tests ==========

    @Test
    @DisplayName("Create vocabulary - Success and verify DB persistence")
    void createVocabulary_Success_SavedInDB() {
        // given
        VocabularyCreateRequest request = new VocabularyCreateRequest(
                "単語",
                "たんご",
                "단어",
                "これは単語です",
                "이것은 단어입니다"
        );

        // when
        VocabularyResponse response = vocabularyService.createVocabulary(testUser1.getUsername(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.word()).isEqualTo("単語");
        assertThat(response.studyStatus()).isEqualTo(StudyStatus.NOT_STUDIED);

        // DB verification
        Vocabulary savedVocabulary = vocabularyRepository.findById(response.id()).orElseThrow();
        assertThat(savedVocabulary.getWord()).isEqualTo("単語");
        assertThat(savedVocabulary.getHiragana()).isEqualTo("たんご");
        assertThat(savedVocabulary.getMeaning()).isEqualTo("단어");
        assertThat(savedVocabulary.getExampleSentence()).isEqualTo("これは単語です");
        assertThat(savedVocabulary.getExampleTranslation()).isEqualTo("이것은 단어입니다");
        assertThat(savedVocabulary.getUser().getId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("Create vocabulary - User not found throws exception")
    void createVocabulary_UserNotFound_ThrowsException() {
        // given
        VocabularyCreateRequest request = new VocabularyCreateRequest(
                "単語", "たんご", "단어", null, null
        );

        // when & then
        assertThatThrownBy(() -> vocabularyService.createVocabulary("nonexistent", request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User not found");
    }

    // ========== Get My Vocabularies Tests ==========

    @Test
    @DisplayName("Get my vocabularies - Returns user's vocabulary list")
    void getMyVocabularies_ReturnsUserList() {
        // given
        createVocabulary(testUser1, "単語1", StudyStatus.NOT_STUDIED);
        createVocabulary(testUser1, "単語2", StudyStatus.STUDYING);
        createVocabulary(testUser2, "単語3", StudyStatus.COMPLETED); // Other user's

        // when
        List<VocabularyListResponse> result = vocabularyService.getMyVocabularies(testUser1.getUsername());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("word")
                .containsExactlyInAnyOrder("単語1", "単語2");
    }

    @Test
    @DisplayName("Get my vocabularies - Returns empty list when no vocabularies")
    void getMyVocabularies_EmptyList() {
        // when
        List<VocabularyListResponse> result = vocabularyService.getMyVocabularies(testUser1.getUsername());

        // then
        assertThat(result).isEmpty();
    }

    // ========== Get Vocabularies By Status Tests ==========

    @Test
    @DisplayName("Get vocabularies by status - Returns only matching status")
    void getVocabulariesByStatus_ReturnsFilteredList() {
        // given
        createVocabulary(testUser1, "単語1", StudyStatus.NOT_STUDIED);
        createVocabulary(testUser1, "単語2", StudyStatus.STUDYING);
        createVocabulary(testUser1, "単語3", StudyStatus.STUDYING);
        createVocabulary(testUser1, "単語4", StudyStatus.COMPLETED);

        // when
        List<VocabularyListResponse> result = vocabularyService
                .getVocabulariesByStatus(testUser1.getUsername(), StudyStatus.STUDYING);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(v -> v.studyStatus() == StudyStatus.STUDYING);
        assertThat(result).extracting("word")
                .containsExactlyInAnyOrder("単語2", "単語3");
    }

    @Test
    @DisplayName("Get vocabularies by status - Returns empty for status with no matches")
    void getVocabulariesByStatus_EmptyForNoMatches() {
        // given
        createVocabulary(testUser1, "単語1", StudyStatus.NOT_STUDIED);

        // when
        List<VocabularyListResponse> result = vocabularyService
                .getVocabulariesByStatus(testUser1.getUsername(), StudyStatus.COMPLETED);

        // then
        assertThat(result).isEmpty();
    }

    // ========== Search Vocabularies Tests ==========

    @Test
    @DisplayName("Search vocabularies - Returns matching results by word")
    void searchVocabularies_MatchingByWord() {
        // given
        createVocabulary(testUser1, "勉強", StudyStatus.NOT_STUDIED);
        createVocabulary(testUser1, "学習", StudyStatus.STUDYING);
        createVocabulary(testUser1, "テスト", StudyStatus.COMPLETED);

        // when
        List<VocabularyListResponse> result = vocabularyService
                .searchVocabularies(testUser1.getUsername(), "勉強");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).word()).isEqualTo("勉強");
    }

    @Test
    @DisplayName("Search vocabularies - Returns matching results by hiragana")
    void searchVocabularies_MatchingByHiragana() {
        // given
        Vocabulary vocab = createVocabulary(testUser1, "勉強", StudyStatus.NOT_STUDIED);
        vocab.update("勉強", "べんきょう", "공부", null, null, StudyStatus.NOT_STUDIED);

        // when
        List<VocabularyListResponse> result = vocabularyService
                .searchVocabularies(testUser1.getUsername(), "べんきょう");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).hiragana()).contains("べんきょう");
    }

    @Test
    @DisplayName("Search vocabularies - Returns matching results by meaning")
    void searchVocabularies_MatchingByMeaning() {
        // given
        createVocabulary(testUser1, "勉強", StudyStatus.NOT_STUDIED);

        // when
        List<VocabularyListResponse> result = vocabularyService
                .searchVocabularies(testUser1.getUsername(), "공부");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).meaning()).contains("공부");
    }

    @Test
    @DisplayName("Search vocabularies - Returns empty for no matches")
    void searchVocabularies_EmptyForNoMatches() {
        // given
        createVocabulary(testUser1, "勉強", StudyStatus.NOT_STUDIED);

        // when
        List<VocabularyListResponse> result = vocabularyService
                .searchVocabularies(testUser1.getUsername(), "존재하지않는단어");

        // then
        assertThat(result).isEmpty();
    }

    // ========== Get Vocabulary Tests ==========

    @Test
    @DisplayName("Get vocabulary - Success")
    void getVocabulary_Success() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when
        VocabularyResponse result = vocabularyService.getVocabulary(vocabulary.getId(), testUser1.getUsername());

        // then
        assertThat(result).isNotNull();
        assertThat(result.word()).isEqualTo("単語");
        assertThat(result.studyStatus()).isEqualTo(StudyStatus.NOT_STUDIED);
    }

    @Test
    @DisplayName("Get vocabulary - Not found throws exception")
    void getVocabulary_NotFound_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> vocabularyService.getVocabulary(999L, testUser1.getUsername()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Vocabulary not found");
    }

    @Test
    @DisplayName("Get vocabulary - Not owner throws exception")
    void getVocabulary_NotOwner_ThrowsException() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        assertThatThrownBy(() -> vocabularyService.getVocabulary(vocabulary.getId(), testUser2.getUsername()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }

    // ========== Update Vocabulary Tests ==========

    @Test
    @DisplayName("Update vocabulary - Success and verify DB update")
    void updateVocabulary_Success_UpdatedInDB() {
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

        // when
        VocabularyResponse result = vocabularyService.updateVocabulary(
                vocabulary.getId(),
                testUser1.getUsername(),
                request
        );

        // then
        assertThat(result.word()).isEqualTo("新しい単語");
        assertThat(result.studyStatus()).isEqualTo(StudyStatus.COMPLETED);

        // DB verification
        Vocabulary updatedVocabulary = vocabularyRepository.findById(vocabulary.getId()).orElseThrow();
        assertThat(updatedVocabulary.getWord()).isEqualTo("新しい単語");
        assertThat(updatedVocabulary.getHiragana()).isEqualTo("あたらしいたんご");
        assertThat(updatedVocabulary.getMeaning()).isEqualTo("새로운 단어");
        assertThat(updatedVocabulary.getExampleSentence()).isEqualTo("新しい例文");
        assertThat(updatedVocabulary.getExampleTranslation()).isEqualTo("새로운 예문");
        assertThat(updatedVocabulary.getStudyStatus()).isEqualTo(StudyStatus.COMPLETED);
    }

    @Test
    @DisplayName("Update vocabulary - Not owner throws exception and DB unchanged")
    void updateVocabulary_NotOwner_ThrowsExceptionAndUnchanged() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);
        String originalWord = vocabulary.getWord();

        VocabularyUpdateRequest request = new VocabularyUpdateRequest(
                "変更された単語", "へんこうされたたんご", "변경된 단어", null, null, StudyStatus.STUDYING
        );

        // when & then
        assertThatThrownBy(() -> vocabularyService.updateVocabulary(
                vocabulary.getId(),
                testUser2.getUsername(),
                request
        ))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        // DB verification - unchanged
        Vocabulary unchangedVocabulary = vocabularyRepository.findById(vocabulary.getId()).orElseThrow();
        assertThat(unchangedVocabulary.getWord()).isEqualTo(originalWord);
        assertThat(unchangedVocabulary.getStudyStatus()).isEqualTo(StudyStatus.NOT_STUDIED);
    }

    // ========== Delete Vocabulary Tests ==========

    @Test
    @DisplayName("Delete vocabulary - Success and verify DB removal")
    void deleteVocabulary_Success_RemovedFromDB() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);
        Long vocabularyId = vocabulary.getId();

        // when
        vocabularyService.deleteVocabulary(vocabularyId, testUser1.getUsername());

        // then - DB verification
        assertThat(vocabularyRepository.findById(vocabularyId)).isEmpty();
    }

    @Test
    @DisplayName("Delete vocabulary - Not owner throws exception and still exists in DB")
    void deleteVocabulary_NotOwner_ThrowsExceptionAndStillExists() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);
        Long vocabularyId = vocabulary.getId();

        // when & then
        assertThatThrownBy(() -> vocabularyService.deleteVocabulary(vocabularyId, testUser2.getUsername()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        // DB verification - still exists
        assertThat(vocabularyRepository.findById(vocabularyId)).isPresent();
    }

    @Test
    @DisplayName("Delete vocabulary - Not found throws exception")
    void deleteVocabulary_NotFound_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> vocabularyService.deleteVocabulary(999L, testUser1.getUsername()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Vocabulary not found");
    }

    // ========== Update Study Status Tests ==========

    @Test
    @DisplayName("Update study status - Success and verify DB update")
    void updateStudyStatus_Success_UpdatedInDB() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when
        VocabularyResponse result = vocabularyService.updateStudyStatus(
                vocabulary.getId(),
                testUser1.getUsername(),
                StudyStatus.COMPLETED
        );

        // then
        assertThat(result.studyStatus()).isEqualTo(StudyStatus.COMPLETED);

        // DB verification
        Vocabulary updatedVocabulary = vocabularyRepository.findById(vocabulary.getId()).orElseThrow();
        assertThat(updatedVocabulary.getStudyStatus()).isEqualTo(StudyStatus.COMPLETED);
    }

    @Test
    @DisplayName("Update study status - Not owner throws exception and DB unchanged")
    void updateStudyStatus_NotOwner_ThrowsExceptionAndUnchanged() {
        // given
        Vocabulary vocabulary = createVocabulary(testUser1, "単語", StudyStatus.NOT_STUDIED);

        // when & then
        assertThatThrownBy(() -> vocabularyService.updateStudyStatus(
                vocabulary.getId(),
                testUser2.getUsername(),
                StudyStatus.COMPLETED
        ))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        // DB verification - unchanged
        Vocabulary unchangedVocabulary = vocabularyRepository.findById(vocabulary.getId()).orElseThrow();
        assertThat(unchangedVocabulary.getStudyStatus()).isEqualTo(StudyStatus.NOT_STUDIED);
    }

    // ========== Full Flow Test ==========

    @Test
    @DisplayName("Full flow - Create -> Get -> Update -> UpdateStatus -> Delete")
    void fullFlow_AllOperations() {
        // 1. Create
        VocabularyCreateRequest createRequest = new VocabularyCreateRequest(
                "単語",
                "たんご",
                "단어",
                "これは単語です",
                "이것은 단어입니다"
        );
        VocabularyResponse created = vocabularyService.createVocabulary(testUser1.getUsername(), createRequest);
        assertThat(created.word()).isEqualTo("単語");

        // 2. Get
        VocabularyResponse retrieved = vocabularyService.getVocabulary(created.id(), testUser1.getUsername());
        assertThat(retrieved.word()).isEqualTo("単語");

        // 3. Update
        VocabularyUpdateRequest updateRequest = new VocabularyUpdateRequest(
                "更新された単語",
                "こうしんされたたんご",
                "업데이트된 단어",
                "新しい例文",
                "새로운 예문",
                StudyStatus.STUDYING
        );
        VocabularyResponse updated = vocabularyService.updateVocabulary(
                created.id(),
                testUser1.getUsername(),
                updateRequest
        );
        assertThat(updated.word()).isEqualTo("更新された単語");
        assertThat(updated.studyStatus()).isEqualTo(StudyStatus.STUDYING);

        // 4. Update Status
        VocabularyResponse statusUpdated = vocabularyService.updateStudyStatus(
                created.id(),
                testUser1.getUsername(),
                StudyStatus.COMPLETED
        );
        assertThat(statusUpdated.studyStatus()).isEqualTo(StudyStatus.COMPLETED);

        // 5. Delete
        vocabularyService.deleteVocabulary(created.id(), testUser1.getUsername());
        assertThat(vocabularyRepository.findById(created.id())).isEmpty();
    }

    // ========== Helper Methods ==========

    private Vocabulary createVocabulary(User user, String word, StudyStatus status) {
        return vocabularyRepository.save(Vocabulary.builder()
                .user(user)
                .word(word)
                .hiragana("ひらがな")
                .meaning("공부")
                .exampleSentence("例文")
                .exampleTranslation("예문")
                .studyStatus(status)
                .build());
    }
}