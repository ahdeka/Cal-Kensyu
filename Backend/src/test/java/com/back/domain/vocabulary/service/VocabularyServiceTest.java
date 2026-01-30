package com.back.domain.vocabulary.service;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import com.back.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.back.domain.vocabulary.dto.request.VocabularyUpdateRequest;
import com.back.domain.vocabulary.dto.response.VocabularyListResponse;
import com.back.domain.vocabulary.dto.response.VocabularyResponse;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import com.back.domain.vocabulary.repository.VocabularyRepository;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VocabularyService Unit Test
 * - Mock-based unit testing
 * - Business logic validation
 * - External dependencies (DB, UserService) isolated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VocabularyService Unit Test")
class VocabularyServiceTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private VocabularyService vocabularyService;

    // ========== Helper Methods ==========

    private User createMockUser(String username) {
        User user = User.builder()
                .username(username)
                .password("password")
                .email(username + "@test.com")
                .nickname(username + "nick")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Vocabulary createMockVocabulary(User user, String word, StudyStatus status) {
        Vocabulary vocabulary = Vocabulary.builder()
                .user(user)
                .word(word)
                .hiragana("ひらがな")
                .meaning("의미")
                .exampleSentence("例文です")
                .exampleTranslation("예문입니다")
                .studyStatus(status)
                .build();
        ReflectionTestUtils.setField(vocabulary, "id", 1L);
        return vocabulary;
    }

    // ========== Create Vocabulary Tests ==========

    @Test
    @DisplayName("Create vocabulary - Success")
    void createVocabulary_Success() {
        // given
        String username = "testuser";
        VocabularyCreateRequest request = new VocabularyCreateRequest(
                "単語",
                "たんご",
                "단어",
                "これは単語です",
                "이것은 단어입니다"
        );

        User mockUser = createMockUser(username);
        Vocabulary mockVocabulary = createMockVocabulary(mockUser, "単語", StudyStatus.NOT_STUDIED);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.save(any(Vocabulary.class))).thenReturn(mockVocabulary);

        // when
        VocabularyResponse result = vocabularyService.createVocabulary(username, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.word()).isEqualTo("単語");
        assertThat(result.studyStatus()).isEqualTo(StudyStatus.NOT_STUDIED);
        verify(vocabularyRepository, times(1)).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("Create vocabulary - User not found throws exception")
    void createVocabulary_UserNotFound_ThrowsException() {
        // given
        String username = "nonexistent";
        VocabularyCreateRequest request = new VocabularyCreateRequest(
                "単語", "たんご", "단어", null, null
        );

        when(userService.getUserByUsername(username))
                .thenThrow(new ServiceException("404", "User not found"));

        // when & then
        assertThatThrownBy(() -> vocabularyService.createVocabulary(username, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("User not found");

        verify(vocabularyRepository, never()).save(any());
    }

    // ========== Get My Vocabularies Tests ==========

    @Test
    @DisplayName("Get my vocabularies - Returns user's vocabulary list")
    void getMyVocabularies_ReturnsUserList() {
        // given
        String username = "testuser";
        User mockUser = createMockUser(username);

        Vocabulary vocab1 = createMockVocabulary(mockUser, "単語1", StudyStatus.NOT_STUDIED);
        Vocabulary vocab2 = createMockVocabulary(mockUser, "単語2", StudyStatus.STUDYING);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.findByUserOrderByCreateDateDesc(mockUser))
                .thenReturn(List.of(vocab1, vocab2));

        // when
        List<VocabularyListResponse> result = vocabularyService.getMyVocabularies(username);

        // then
        assertThat(result).hasSize(2);
        verify(vocabularyRepository, times(1)).findByUserOrderByCreateDateDesc(mockUser);
    }

    @Test
    @DisplayName("Get my vocabularies - Returns empty list when no vocabularies")
    void getMyVocabularies_EmptyList() {
        // given
        String username = "testuser";
        User mockUser = createMockUser(username);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.findByUserOrderByCreateDateDesc(mockUser))
                .thenReturn(List.of());

        // when
        List<VocabularyListResponse> result = vocabularyService.getMyVocabularies(username);

        // then
        assertThat(result).isEmpty();
    }

    // ========== Get Vocabularies By Status Tests ==========

    @Test
    @DisplayName("Get vocabularies by status - Returns filtered list")
    void getVocabulariesByStatus_ReturnsFilteredList() {
        // given
        String username = "testuser";
        User mockUser = createMockUser(username);
        StudyStatus status = StudyStatus.STUDYING;

        Vocabulary vocab1 = createMockVocabulary(mockUser, "単語1", StudyStatus.STUDYING);
        Vocabulary vocab2 = createMockVocabulary(mockUser, "単語2", StudyStatus.STUDYING);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.findByUserAndStudyStatusOrderByCreateDateDesc(mockUser, status))
                .thenReturn(List.of(vocab1, vocab2));

        // when
        List<VocabularyListResponse> result = vocabularyService.getVocabulariesByStatus(username, status);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(v -> v.studyStatus() == StudyStatus.STUDYING);
    }

    @Test
    @DisplayName("Get vocabularies by status - Returns empty for status with no matches")
    void getVocabulariesByStatus_EmptyForNoMatches() {
        // given
        String username = "testuser";
        User mockUser = createMockUser(username);
        StudyStatus status = StudyStatus.COMPLETED;

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.findByUserAndStudyStatusOrderByCreateDateDesc(mockUser, status))
                .thenReturn(List.of());

        // when
        List<VocabularyListResponse> result = vocabularyService.getVocabulariesByStatus(username, status);

        // then
        assertThat(result).isEmpty();
    }

    // ========== Search Vocabularies Tests ==========

    @Test
    @DisplayName("Search vocabularies - Returns matching results")
    void searchVocabularies_ReturnsMatchingResults() {
        // given
        String username = "testuser";
        String keyword = "単語";
        User mockUser = createMockUser(username);

        Vocabulary vocab1 = createMockVocabulary(mockUser, "単語1", StudyStatus.NOT_STUDIED);
        Vocabulary vocab2 = createMockVocabulary(mockUser, "単語2", StudyStatus.STUDYING);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.searchByKeyword(mockUser, keyword))
                .thenReturn(List.of(vocab1, vocab2));

        // when
        List<VocabularyListResponse> result = vocabularyService.searchVocabularies(username, keyword);

        // then
        assertThat(result).hasSize(2);
        verify(vocabularyRepository, times(1)).searchByKeyword(mockUser, keyword);
    }

    @Test
    @DisplayName("Search vocabularies - Returns empty for no matches")
    void searchVocabularies_EmptyForNoMatches() {
        // given
        String username = "testuser";
        String keyword = "존재하지않는단어";
        User mockUser = createMockUser(username);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(vocabularyRepository.searchByKeyword(mockUser, keyword))
                .thenReturn(List.of());

        // when
        List<VocabularyListResponse> result = vocabularyService.searchVocabularies(username, keyword);

        // then
        assertThat(result).isEmpty();
    }

    // ========== Get Vocabulary Tests ==========

    @Test
    @DisplayName("Get vocabulary - Success")
    void getVocabulary_Success() {
        // given
        Long vocabularyId = 1L;
        String username = "owner";
        User owner = createMockUser(username);
        Vocabulary vocabulary = createMockVocabulary(owner, "単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));

        // when
        VocabularyResponse result = vocabularyService.getVocabulary(vocabularyId, username);

        // then
        assertThat(result).isNotNull();
        assertThat(result.word()).isEqualTo("単語");
        verify(userService, times(1)).validateOwnership(owner, username);
    }

    @Test
    @DisplayName("Get vocabulary - Not found throws exception")
    void getVocabulary_NotFound_ThrowsException() {
        // given
        Long vocabularyId = 999L;
        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> vocabularyService.getVocabulary(vocabularyId, "user"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Vocabulary not found");
    }

    @Test
    @DisplayName("Get vocabulary - Not owner throws exception")
    void getVocabulary_NotOwner_ThrowsException() {
        // given
        Long vocabularyId = 1L;
        String owner = "owner";
        String otherUser = "otheruser";

        User ownerUser = createMockUser(owner);
        Vocabulary vocabulary = createMockVocabulary(ownerUser, "単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));
        doThrow(new ServiceException("403", "You do not have permission to perform this operation"))
                .when(userService).validateOwnership(ownerUser, otherUser);

        // when & then
        assertThatThrownBy(() -> vocabularyService.getVocabulary(vocabularyId, otherUser))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }

    // ========== Update Vocabulary Tests ==========

    @Test
    @DisplayName("Update vocabulary - Success")
    void updateVocabulary_Success() {
        // given
        Long vocabularyId = 1L;
        String username = "owner";

        VocabularyUpdateRequest request = new VocabularyUpdateRequest(
                "更新された単語",
                "こうしんされたたんご",
                "업데이트된 단어",
                "新しい例文",
                "새로운 예문",
                StudyStatus.COMPLETED
        );

        User owner = createMockUser(username);
        Vocabulary vocabulary = createMockVocabulary(owner, "古い単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));

        // when
        VocabularyResponse result = vocabularyService.updateVocabulary(vocabularyId, username, request);

        // then
        assertThat(result).isNotNull();
        verify(userService, times(1)).validateOwnership(owner, username);
    }

    @Test
    @DisplayName("Update vocabulary - Not owner throws exception")
    void updateVocabulary_NotOwner_ThrowsException() {
        // given
        Long vocabularyId = 1L;
        String owner = "owner";
        String otherUser = "otheruser";

        VocabularyUpdateRequest request = new VocabularyUpdateRequest(
                "単語", "たんご", "단어", null, null, StudyStatus.STUDYING
        );

        User ownerUser = createMockUser(owner);
        Vocabulary vocabulary = createMockVocabulary(ownerUser, "単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));
        doThrow(new ServiceException("403", "You do not have permission to perform this operation"))
                .when(userService).validateOwnership(ownerUser, otherUser);

        // when & then
        assertThatThrownBy(() -> vocabularyService.updateVocabulary(vocabularyId, otherUser, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }

    // ========== Delete Vocabulary Tests ==========

    @Test
    @DisplayName("Delete vocabulary - Success")
    void deleteVocabulary_Success() {
        // given
        Long vocabularyId = 1L;
        String username = "owner";

        User owner = createMockUser(username);
        Vocabulary vocabulary = createMockVocabulary(owner, "単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));

        // when
        vocabularyService.deleteVocabulary(vocabularyId, username);

        // then
        verify(userService, times(1)).validateOwnership(owner, username);
        verify(vocabularyRepository, times(1)).delete(vocabulary);
    }

    @Test
    @DisplayName("Delete vocabulary - Not owner throws exception")
    void deleteVocabulary_NotOwner_ThrowsException() {
        // given
        Long vocabularyId = 1L;
        String owner = "owner";
        String otherUser = "otheruser";

        User ownerUser = createMockUser(owner);
        Vocabulary vocabulary = createMockVocabulary(ownerUser, "単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));
        doThrow(new ServiceException("403", "You do not have permission to perform this operation"))
                .when(userService).validateOwnership(ownerUser, otherUser);

        // when & then
        assertThatThrownBy(() -> vocabularyService.deleteVocabulary(vocabularyId, otherUser))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        verify(vocabularyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete vocabulary - Not found throws exception")
    void deleteVocabulary_NotFound_ThrowsException() {
        // given
        Long vocabularyId = 999L;
        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> vocabularyService.deleteVocabulary(vocabularyId, "user"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Vocabulary not found");

        verify(vocabularyRepository, never()).delete(any());
    }

    // ========== Update Study Status Tests ==========

    @Test
    @DisplayName("Update study status - Success")
    void updateStudyStatus_Success() {
        // given
        Long vocabularyId = 1L;
        String username = "owner";
        StudyStatus newStatus = StudyStatus.COMPLETED;

        User owner = createMockUser(username);
        Vocabulary vocabulary = createMockVocabulary(owner, "単語", StudyStatus.STUDYING);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));

        // when
        VocabularyResponse result = vocabularyService.updateStudyStatus(vocabularyId, username, newStatus);

        // then
        assertThat(result).isNotNull();
        verify(userService, times(1)).validateOwnership(owner, username);
    }

    @Test
    @DisplayName("Update study status - Not owner throws exception")
    void updateStudyStatus_NotOwner_ThrowsException() {
        // given
        Long vocabularyId = 1L;
        String owner = "owner";
        String otherUser = "otheruser";
        StudyStatus newStatus = StudyStatus.COMPLETED;

        User ownerUser = createMockUser(owner);
        Vocabulary vocabulary = createMockVocabulary(ownerUser, "単語", StudyStatus.NOT_STUDIED);

        when(vocabularyRepository.findById(vocabularyId)).thenReturn(Optional.of(vocabulary));
        doThrow(new ServiceException("403", "You do not have permission to perform this operation"))
                .when(userService).validateOwnership(ownerUser, otherUser);

        // when & then
        assertThatThrownBy(() -> vocabularyService.updateStudyStatus(vocabularyId, otherUser, newStatus))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }
}