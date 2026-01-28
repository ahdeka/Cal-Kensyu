package com.back.domain.diary.service;

import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.dto.response.DiaryListResponse;
import com.back.domain.diary.dto.response.DiaryResponse;
import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserService userService;

    private static final int MAX_YEARS_BACK = 1;

    @Transactional
    public DiaryResponse createDiary(String username, DiaryCreateRequest request) {
        User user = userService.getUserByUsername(username);

        validateDiaryDate(request.diaryDate());
        validateDuplicateDiaryDate(user, request.diaryDate());

        Diary diary = Diary.builder()
                .user(user)
                .diaryDate(request.diaryDate())
                .title(request.title())
                .content(request.content())
                .isPublic(request.isPublic())
                .build();

        Diary savedDiary = diaryRepository.save(diary);

        return DiaryResponse.from(savedDiary);
    }

    public List<DiaryListResponse> getPublicDiaries() {
        List<Diary> diaries = diaryRepository.findByIsPublicTrueOrderByCreateDateDesc();

        return diaries.stream()
                .map(DiaryListResponse::from)
                .collect(Collectors.toList());
    }

    public List<DiaryListResponse> getMyDiaries(String username) {
        User user = userService.getUserByUsername(username);

        List<Diary> diaries = diaryRepository.findByUserOrderByCreateDateDesc(user);

        return diaries.stream()
                .map(DiaryListResponse::from)
                .collect(Collectors.toList());
    }

    public DiaryResponse getDiary(Long diaryId, String username) {
        Diary diary = getDiaryById(diaryId);

        validateViewPermission(diary, username);

        return DiaryResponse.from(diary);
    }

    @Transactional
    public DiaryResponse updateDiary(Long diaryId, String username, DiaryUpdateRequest request) {
        Diary diary = getDiaryById(diaryId);

        userService.validateOwnership(diary.getUser(), username);

        // Validate new diary date if changed
        if (!diary.getDiaryDate().equals(request.diaryDate())) {
            validateDiaryDate(request.diaryDate());

            // Check if new date conflicts with existing diary
            diaryRepository.findByUserAndDiaryDate(diary.getUser(), request.diaryDate())
                    .ifPresent(existingDiary -> {
                        throw new ServiceException("400", "A diary entry for this date already exists");
                    });
        }

        diary.update(request.title(), request.content(), request.diaryDate(), request.isPublic());

        return DiaryResponse.from(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId, String username) {
        Diary diary = getDiaryById(diaryId);

        userService.validateOwnership(diary.getUser(), username);

        diaryRepository.delete(diary);
    }

    private Diary getDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId)
                .orElseThrow(() -> new ServiceException("404", "Diary not found"));
    }


    // ===== Helper Method ===== //

    private void validateDiaryDate(LocalDate diaryDate) {
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(MAX_YEARS_BACK);

        if (diaryDate.isAfter(today)) {
            throw new ServiceException("400", "Cannot set a future date");
        }

        if (diaryDate.isBefore(oneYearAgo)) {
            throw new ServiceException("400", "Cannot set a date more than one year ago");
        }
    }

    private void validateViewPermission(Diary diary, String username) {
        // Public diary - anyone can view
        if (diary.isPublic()) {
            return;
        }

        // Private diary - only owner can view
        if (username == null) {
            throw new ServiceException("401", "Authentication required to view private diary");
        }

        if (!diary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "You do not have permission to view this diary");
        }
    }

    private void validateDuplicateDiaryDate(User user, LocalDate diaryDate) {
        diaryRepository.findByUserAndDiaryDate(user, diaryDate)
                .ifPresent(diary -> {
                    throw new ServiceException("400", "A diary entry for this date already exists");
                });
    }
}