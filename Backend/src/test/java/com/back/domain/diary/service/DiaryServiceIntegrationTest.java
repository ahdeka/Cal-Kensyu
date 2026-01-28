package com.back.domain.diary.service;

import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.dto.response.DiaryListResponse;
import com.back.domain.diary.dto.response.DiaryResponse;
import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * DiaryService Integration Test
 * - Real Spring Context
 * - Real H2 Database
 * - Full Service + Repository + DB integration
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("DiaryService Integration Test")
class DiaryServiceIntegrationTest {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private DiaryRepository diaryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = User.builder()
                .username("user1")
                .password(passwordEncoder.encode("password"))
                .email("user1@test.com")
                .nickname("user1nick")
                .role(Role.USER)
                .build();
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .username("user2")
                .password(passwordEncoder.encode("password"))
                .email("user2@test.com")
                .nickname("user2nick")
                .role(Role.USER)
                .build();
        testUser2 = userRepository.save(testUser2);
    }

    @AfterEach
    void tearDown() {
        diaryRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Create Diary Tests ==========

    @Test
    @DisplayName("Create diary - Success and persisted in DB")
    void createDiary_Success_PersistedInDB() {
        // given
        LocalDate diaryDate = LocalDate.now().minusDays(1);
        DiaryCreateRequest request = new DiaryCreateRequest(
                diaryDate,
                "Integration Test Diary",
                "This is an integration test diary entry",
                true
        );

        // when
        DiaryResponse response = diaryService.createDiary(testUser1.getUsername(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("Integration Test Diary");
        assertThat(response.username()).isEqualTo("user1");

        // Verify DB persistence
        Diary savedDiary = diaryRepository.findById(response.id()).orElseThrow();
        assertThat(savedDiary.getTitle()).isEqualTo("Integration Test Diary");
        assertThat(savedDiary.getContent()).isEqualTo("This is an integration test diary entry");
        assertThat(savedDiary.getDiaryDate()).isEqualTo(diaryDate);
        assertThat(savedDiary.isPublic()).isTrue();
    }

    @Test
    @DisplayName("Create diary - Duplicate date throws exception")
    void createDiary_DuplicateDate_ThrowsException() {
        // given
        LocalDate diaryDate = LocalDate.now().minusDays(1);
        DiaryCreateRequest request1 = new DiaryCreateRequest(
                diaryDate, "First Diary", "Content", true
        );
        DiaryCreateRequest request2 = new DiaryCreateRequest(
                diaryDate, "Second Diary", "Content", true
        );

        diaryService.createDiary(testUser1.getUsername(), request1);

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(testUser1.getUsername(), request2))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("A diary entry for this date already exists");

        // Verify only one diary exists in DB
        List<Diary> diaries = diaryRepository.findByUserOrderByCreateDateDesc(testUser1);
        assertThat(diaries).hasSize(1);
    }

    @Test
    @DisplayName("Create diary - Future date throws exception")
    void createDiary_FutureDate_ThrowsException() {
        // given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        DiaryCreateRequest request = new DiaryCreateRequest(
                futureDate, "Future Diary", "Content", true
        );

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(testUser1.getUsername(), request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Cannot set a future date");

        // Verify no diary created in DB
        assertThat(diaryRepository.count()).isZero();
    }

    @Test
    @DisplayName("Create diary - Date too old throws exception")
    void createDiary_DateTooOld_ThrowsException() {
        // given
        LocalDate oldDate = LocalDate.now().minusYears(2);
        DiaryCreateRequest request = new DiaryCreateRequest(
                oldDate, "Old Diary", "Content", true
        );

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(testUser1.getUsername(), request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Cannot set a date more than");

        // Verify no diary created in DB
        assertThat(diaryRepository.count()).isZero();
    }

    // ========== Get Public Diaries Tests ==========

    @Test
    @DisplayName("Get public diaries - Returns only public diaries")
    void getPublicDiaries_ReturnsOnlyPublicDiaries() {
        // given
        createDiary(testUser1, LocalDate.now().minusDays(1), "Public Diary 1", true);
        createDiary(testUser1, LocalDate.now().minusDays(2), "Private Diary", false);
        createDiary(testUser2, LocalDate.now().minusDays(3), "Public Diary 2", true);

        // when
        List<DiaryListResponse> result = diaryService.getPublicDiaries();

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(DiaryListResponse::title)
                .containsExactlyInAnyOrder("Public Diary 1", "Public Diary 2");
    }

    @Test
    @DisplayName("Get public diaries - Returns empty when no public diaries")
    void getPublicDiaries_EmptyList() {
        // given
        createDiary(testUser1, LocalDate.now(), "Private Diary", false);

        // when
        List<DiaryListResponse> result = diaryService.getPublicDiaries();

        // then
        assertThat(result).isEmpty();
    }

    // ========== Get My Diaries Tests ==========

    @Test
    @DisplayName("Get my diaries - Returns all user's diaries")
    void getMyDiaries_ReturnsAllUserDiaries() {
        // given
        createDiary(testUser1, LocalDate.now().minusDays(1), "My Public", true);
        createDiary(testUser1, LocalDate.now().minusDays(2), "My Private", false);
        createDiary(testUser2, LocalDate.now().minusDays(3), "Other User", true);

        // when
        List<DiaryListResponse> result = diaryService.getMyDiaries(testUser1.getUsername());

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(DiaryListResponse::title)
                .containsExactlyInAnyOrder("My Public", "My Private");
    }

    @Test
    @DisplayName("Get my diaries - Returns both public and private")
    void getMyDiaries_ReturnsBothPublicAndPrivate() {
        // given
        createDiary(testUser1, LocalDate.now().minusDays(1), "Public", true);
        createDiary(testUser1, LocalDate.now().minusDays(2), "Private", false);

        // when
        List<DiaryListResponse> result = diaryService.getMyDiaries(testUser1.getUsername());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(DiaryListResponse::isPublic)
                .containsExactlyInAnyOrder(true, false);
    }

    // ========== Get Diary Tests ==========

    @Test
    @DisplayName("Get diary - Public diary viewable by anyone")
    void getDiary_PublicDiary_ViewableByAnyone() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Public", true);

        // when
        DiaryResponse result = diaryService.getDiary(diary.getId(), null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Public");
        assertThat(result.isPublic()).isTrue();
    }

    @Test
    @DisplayName("Get diary - Private diary viewable by owner")
    void getDiary_PrivateDiary_OwnerCanView() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Private", false);

        // when
        DiaryResponse result = diaryService.getDiary(diary.getId(), testUser1.getUsername());

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Private");
        assertThat(result.isPublic()).isFalse();
    }

    @Test
    @DisplayName("Get diary - Private diary not viewable without auth")
    void getDiary_PrivateDiary_NoAuth_ThrowsException() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Private", false);

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(diary.getId(), null))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    @DisplayName("Get diary - Private diary not viewable by others")
    void getDiary_PrivateDiary_OtherUser_ThrowsException() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Private", false);

        // when & then
        assertThatThrownBy(() -> diaryService.getDiary(diary.getId(), testUser2.getUsername()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");
    }

    // ========== Update Diary Tests ==========

    @Test
    @DisplayName("Update diary - Success and persisted in DB")
    void updateDiary_Success_PersistedInDB() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now().minusDays(2), "Original", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.now().minusDays(1),
                "Updated Title",
                "Updated Content",
                false
        );

        // when
        DiaryResponse result = diaryService.updateDiary(
                diary.getId(),
                testUser1.getUsername(),
                request
        );

        // then
        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(result.content()).isEqualTo("Updated Content");
        assertThat(result.isPublic()).isFalse();

        // Verify DB persistence
        Diary updatedDiary = diaryRepository.findById(diary.getId()).orElseThrow();
        assertThat(updatedDiary.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedDiary.getContent()).isEqualTo("Updated Content");
        assertThat(updatedDiary.getDiaryDate()).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(updatedDiary.isPublic()).isFalse();
    }

    @Test
    @DisplayName("Update diary - Date change without conflict")
    void updateDiary_DateChange_NoConflict() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now().minusDays(2), "Diary", true);
        LocalDate newDate = LocalDate.now().minusDays(1);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                newDate, "Updated", "Content", true
        );

        // when
        DiaryResponse result = diaryService.updateDiary(
                diary.getId(),
                testUser1.getUsername(),
                request
        );

        // then
        assertThat(result.diaryDate()).isEqualTo(newDate);

        // Verify in DB
        Diary updatedDiary = diaryRepository.findById(diary.getId()).orElseThrow();
        assertThat(updatedDiary.getDiaryDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("Update diary - Date conflict throws exception")
    void updateDiary_DateConflict_ThrowsException() {
        // given
        LocalDate conflictDate = LocalDate.now().minusDays(1);
        Diary diary1 = createDiary(testUser1, LocalDate.now().minusDays(2), "Diary 1", true);
        createDiary(testUser1, conflictDate, "Diary 2", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                conflictDate, "Updated", "Content", true
        );

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(
                diary1.getId(),
                testUser1.getUsername(),
                request
        ))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("A diary entry for this date already exists");

        // Verify original diary unchanged in DB
        Diary unchangedDiary = diaryRepository.findById(diary1.getId()).orElseThrow();
        assertThat(unchangedDiary.getTitle()).isEqualTo("Diary 1");
    }

    @Test
    @DisplayName("Update diary - Not owner throws exception")
    void updateDiary_NotOwner_ThrowsException() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Diary", true);

        DiaryUpdateRequest request = new DiaryUpdateRequest(
                LocalDate.now(), "Updated", "Content", true
        );

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(
                diary.getId(),
                testUser2.getUsername(),
                request
        ))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        // Verify diary unchanged in DB
        Diary unchangedDiary = diaryRepository.findById(diary.getId()).orElseThrow();
        assertThat(unchangedDiary.getTitle()).isEqualTo("Diary");
    }

    // ========== Delete Diary Tests ==========

    @Test
    @DisplayName("Delete diary - Success and removed from DB")
    void deleteDiary_Success_RemovedFromDB() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "To Delete", true);
        Long diaryId = diary.getId();

        // when
        diaryService.deleteDiary(diaryId, testUser1.getUsername());

        // then
        assertThat(diaryRepository.findById(diaryId)).isEmpty();
        assertThat(diaryRepository.count()).isZero();
    }

    @Test
    @DisplayName("Delete diary - Not owner throws exception")
    void deleteDiary_NotOwner_ThrowsException() {
        // given
        Diary diary = createDiary(testUser1, LocalDate.now(), "Diary", true);

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(
                diary.getId(),
                testUser2.getUsername()
        ))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("You do not have permission");

        // Verify diary still exists in DB
        assertThat(diaryRepository.findById(diary.getId())).isPresent();
    }

    @Test
    @DisplayName("Delete diary - Not found throws exception")
    void deleteDiary_NotFound_ThrowsException() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(
                nonExistentId,
                testUser1.getUsername()
        ))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Diary not found");
    }

    // ========== Full Flow Test ==========

    @Test
    @DisplayName("Full flow - Create -> Get -> Update -> Delete")
    void fullFlow_CreateGetUpdateDelete() {
        // 1. Create
        DiaryCreateRequest createRequest = new DiaryCreateRequest(
                LocalDate.now().minusDays(1),
                "Flow Test",
                "Initial content",
                true
        );
        DiaryResponse created = diaryService.createDiary(testUser1.getUsername(), createRequest);
        assertThat(created.id()).isNotNull();

        // 2. Get
        DiaryResponse retrieved = diaryService.getDiary(created.id(), testUser1.getUsername());
        assertThat(retrieved.title()).isEqualTo("Flow Test");

        // 3. Update
        DiaryUpdateRequest updateRequest = new DiaryUpdateRequest(
                LocalDate.now().minusDays(1),
                "Updated Flow",
                "Updated content",
                false
        );
        DiaryResponse updated = diaryService.updateDiary(
                created.id(),
                testUser1.getUsername(),
                updateRequest
        );
        assertThat(updated.title()).isEqualTo("Updated Flow");
        assertThat(updated.isPublic()).isFalse();

        // 4. Delete
        diaryService.deleteDiary(created.id(), testUser1.getUsername());
        assertThat(diaryRepository.findById(created.id())).isEmpty();
    }

    // ========== Helper Methods ==========

    private Diary createDiary(User user, LocalDate diaryDate, String title, boolean isPublic) {
        Diary diary = Diary.builder()
                .user(user)
                .diaryDate(diaryDate)
                .title(title)
                .content("Test content for " + title)
                .isPublic(isPublic)
                .build();
        return diaryRepository.save(diary);
    }
}