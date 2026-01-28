package com.back.domain.diary.service;

import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.dto.response.DiaryListResponse;
import com.back.domain.diary.dto.response.DiaryResponse;
import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DiaryService Unit Test
 * - Mock-based unit testing
 * - Business logic validation
 * - External dependencies (DB, UserService) isolated
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiaryService Unit Test")
class DiaryServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DiaryService diaryService;

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

    private Diary createMockDiary(User user, LocalDate diaryDate, boolean isPublic) {
        Diary diary = Diary.builder()
                .user(user)
                .diaryDate(diaryDate)
                .title("Test Title")
                .content("Test Content")
                .isPublic(isPublic)
                .build();
        ReflectionTestUtils.setField(diary, "id", 1L);
        return diary;
    }

    // ========== Create Diary Tests ==========

    @Test
    @DisplayName("Create diary - Success")
    void createDiary_Success() {
        // given
        String username = "testuser";
        LocalDate diaryDate = LocalDate.now().minusDays(1);
        DiaryCreateRequest request = new DiaryCreateRequest(
                diaryDate,
                "My First Diary",
                "Today was a good day",
                true
        );

        User mockUser = createMockUser(username);
        Diary mockDiary = createMockDiary(mockUser, diaryDate, true);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(diaryRepository.findByUserAndDiaryDate(mockUser, diaryDate))
                .thenReturn(Optional.empty());
        when(diaryRepository.save(any(Diary.class))).thenReturn(mockDiary);

        // when
        DiaryResponse result = diaryService.createDiary(username, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Title");
        verify(diaryRepository, times(1)).save(any(Diary.class));
    }

    @Test
    @DisplayName("Create diary - Duplicate date throws exception")
    void createDiary_DuplicateDate_ThrowsException() {
        // given
        String username = "testuser";
        LocalDate diaryDate = LocalDate.now().minusDays(1);
        DiaryCreateRequest request = new DiaryCreateRequest(
                diaryDate,
                "Title",
                "Content",
                true
        );

        User mockUser = createMockUser(username);
        Diary existingDiary = createMockDiary(mockUser, diaryDate, true);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(diaryRepository.findByUserAndDiaryDate(mockUser, diaryDate))
                .thenReturn(Optional.of(existingDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(username, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("A diary entry for this date already exists");

        verify(diaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create diary - Future date throws exception")
    void createDiary_FutureDate_ThrowsException() {
        // given
        String username = "testuser";
        LocalDate futureDate = LocalDate.now().plusDays(1);
        DiaryCreateRequest request = new DiaryCreateRequest(
                futureDate,
                "Title",
                "Content",
                true
        );

        User mockUser = createMockUser(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(username, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Cannot set a future date");

        verify(diaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create diary - Date too old throws exception")
    void createDiary_DateTooOld_ThrowsException() {
        // given
        String username = "testuser";
        LocalDate oldDate = LocalDate.now().minusYears(2);
        DiaryCreateRequest request = new DiaryCreateRequest(
                oldDate,
                "Title",
                "Content",
                true
        );

        User mockUser = createMockUser(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(username, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Cannot set a date more than");

        verify(diaryRepository, never()).save(any());
    }

    // ========== Get Public Diaries Tests ==========

    @Test
    @DisplayName("Get public diaries - Returns list")
    void getPublicDiaries_ReturnsLis() {
        // given
        User user1 = createMockUser("user1");
        User user2 = createMockUser("user2");

        Diary diary1 = createMockDiary(user1, LocalDate.now(), true);
        Diary diary2 = createMockDiary(user2, LocalDate.now().minusDays(1), true);

        when(diaryRepository.findByIsPublicTrueOrderByCreateDateDesc())
                .thenReturn(List.of(diary1, diary2));

        // when
        List<DiaryListResponse> result = diaryService.getPublicDiaries();

        // then
        assertThat(result).hasSize(2);
        verify(diaryRepository, times(1)).findByIsPublicTrueOrderByCreateDateDesc();
    }

    @Test
    @DisplayName("Get public diaries - Returns empty list when no public diaries")
    void getPublicDiaries_EmptyList() {
        // given
        when(diaryRepository.findByIsPublicTrueOrderByCreateDateDesc())
                .thenReturn(List.of());

        // when
        List<DiaryListResponse> result = diaryService.getPublicDiaries();

        // then
        assertThat(result).isEmpty();
    }

    // ========== Get My Diaries Tests ==========

    @Test
    @DisplayName("Get my diaries - Returns user's diaries")
    void getMyDiaries_ReturnsUserDiaries() {
        // given
        String username = "testuser";
        User mockUser = createMockUser(username);

        Diary diary1 = createMockDiary(mockUser, LocalDate.now(), true);
        Diary diary2 = createMockDiary(mockUser, LocalDate.now().minusDays(1), false);

        when(userService.getUserByUsername(username)).thenReturn(mockUser);
        when(diaryRepository.findByUserOrderByCreateDateDesc(mockUser))
                .thenReturn(List.of(diary1, diary2));

        // when
        List<DiaryListResponse> result = diaryService.getMyDiaries(username);

        // then
        assertThat(result).hasSize(2);
        verify(diaryRepository, times(1)).findByUserOrderByCreateDateDesc(mockUser);
    }

    // ========== Get Diary Tests ==========

    @Test
    @DisplayName("Get diary - Public diary can be viewed by anyone")
    void getDiary_PublicDiary_Success() {
        // given
        Long diaryId = 1L;
        User owner = createMockUser("owner");
        Diary publicDiary = createMockDiary(owner, LocalDate.now(), true);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(publicDiary));

        // when
        DiaryResponse result = diaryService.getDiary(diaryId, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isPublic()).isTrue();
    }

    @Test
    @DisplayName("Get diary - Private diary can be viewed by owner")
    void getDiary_PrivateDiary_OwnerCanView() {
        // given
        Long diaryId = 1L;
        String username = "owner";
        User owner = createMockUser(username);
        Diary privateDiary = createMockDiary(owner, LocalDate.now(), false);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(privateDiary));

        // when
        DiaryResponse result = diaryService.getDiary(diaryId, username);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isPublic()).isFalse();
    }

    @Test
    @DisplayName("Get diary - Private diary cannot be viewed without authentication")
    void getDiary_PrivateDiary_NoAuth_ThrowsException() {
        // given
        Long diaryId = 1L;
        User owner = createMockUser("owner");
        Diary privateDiary = createMockDiary(owner, LocalDate.now(), false);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(privateDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(diaryId, null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    @DisplayName("Get diary - Private diary cannot be viewed by others")
    void getDiary_PrivateDiary_OtherUser_ThrowsException() {
        // given
        Long diaryId = 1L;
        User owner = createMockUser("owner");
        Diary privateDiary = createMockDiary(owner, LocalDate.now(), false);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(privateDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(diaryId, "otheruser"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }

    @Test
    @DisplayName("Get diary - Diary not found throws exception")
    void getDiary_NotFound_ThrowsException() {
        // given
        Long diaryId = 999L;
        when(diaryRepository.findById(diaryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(diaryId, "user"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Diary not found");
    }

    // ========== Update Diary Tests ==========

    @Test
    @DisplayName("Update diary - Success")
    void updateDiary_Success() {
        // given
        Long diaryId = 1L;
        String username = "owner";
        LocalDate newDate = LocalDate.now().minusDays(1);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                newDate,
                "Updated Title",
                "Updated Content",
                false
        );

        User owner = createMockUser(username);
        Diary diary = createMockDiary(owner, LocalDate.now().minusDays(2), true);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(diaryRepository.findByUserAndDiaryDate(owner, newDate))
                .thenReturn(Optional.empty());

        // when
        DiaryResponse result = diaryService.updateDiary(diaryId, username, request);

        // then
        assertThat(result).isNotNull();
        verify(userService, times(1)).validateOwnership(owner, username);
    }

    @Test
    @DisplayName("Update diary - Date conflict throws exception")
    void updateDiary_DateConflict_ThrowsException() {
        // given
        Long diaryId = 1L;
        String username = "owner";
        LocalDate conflictDate = LocalDate.now().minusDays(1);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                conflictDate,
                "Updated Title",
                "Updated Content",
                false
        );

        User owner = createMockUser(username);
        Diary diary = createMockDiary(owner, LocalDate.now().minusDays(2), true);
        Diary conflictDiary = createMockDiary(owner, conflictDate, true);
        ReflectionTestUtils.setField(conflictDiary, "id", 2L);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        when(diaryRepository.findByUserAndDiaryDate(owner, conflictDate))
                .thenReturn(Optional.of(conflictDiary));

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(diaryId, username, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("A diary entry for this date already exists");
    }

    @Test
    @DisplayName("Update diary - Not owner throws exception")
    void updateDiary_NotOwner_ThrowsException() {
        // given
        Long diaryId = 1L;
        String owner = "owner";
        String otherUser = "otheruser";

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.now(),
                "Title",
                "Content",
                true
        );

        User ownerUser = createMockUser(owner);
        Diary diary = createMockDiary(ownerUser, LocalDate.now(), true);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        doThrow(new ServiceException("403", "You do not have permission to perform this operation"))
                .when(userService).validateOwnership(ownerUser, otherUser);

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(diaryId, otherUser, request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }

    // ========== Delete Diary Tests ==========

    @Test
    @DisplayName("Delete diary - Success")
    void deleteDiary_Success() {
        // given
        Long diaryId = 1L;
        String username = "owner";

        User owner = createMockUser(username);
        Diary diary = createMockDiary(owner, LocalDate.now(), true);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));

        // when
        diaryService.deleteDiary(diaryId, username);

        // then
        verify(userService, times(1)).validateOwnership(owner, username);
        verify(diaryRepository, times(1)).delete(diary);
    }

    @Test
    @DisplayName("Delete diary - Not owner throws exception")
    void deleteDiary_NotOwner_ThrowsException() {
        // given
        Long diaryId = 1L;
        String owner = "owner";
        String otherUser = "otheruser";

        User ownerUser = createMockUser(owner);
        Diary diary = createMockDiary(ownerUser, LocalDate.now(), true);

        when(diaryRepository.findById(diaryId)).thenReturn(Optional.of(diary));
        doThrow(new ServiceException("403", "You do not have permission to perform this operation"))
                .when(userService).validateOwnership(ownerUser, otherUser);

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(diaryId, otherUser))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        verify(diaryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete diary - Diary not found throws exception")
    void deleteDiary_NotFound_ThrowsException() {
        // given
        Long diaryId = 999L;
        when(diaryRepository.findById(diaryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(diaryId, "user"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Diary not found");

        verify(diaryRepository, never()).delete(any());
    }
}