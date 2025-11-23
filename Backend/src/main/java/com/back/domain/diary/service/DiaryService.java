package com.back.domain.diary.service;


import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.dto.response.DiaryListResponse;
import com.back.domain.diary.dto.response.DiaryResponse;
import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;

    @Transactional
    public DiaryResponse createDiary(String username, DiaryCreateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        diaryRepository.findByUserAndDiaryDate(user, request.diaryDate())
                .ifPresent(diary -> {
                    throw new ServiceException("400", "この日付の日記は既に存在します");
                });

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
        List<Diary> diaries = diaryRepository.findByIsPublicTrueOrderByDiaryDateDesc();

        return diaries.stream()
                .map(DiaryListResponse::from)
                .collect(Collectors.toList());
    }

    public List<DiaryListResponse> getMyDiaries(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        List<Diary> diaries = diaryRepository.findByUserOrderByDiaryDateDesc(user);

        return diaries.stream()
                .map(DiaryListResponse::from)
                .collect(Collectors.toList());
    }

    public DiaryResponse getDiary(Long diaryId, String username) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new ServiceException("404", "日記が見つかりません"));

        if (!diary.isPublic() && !diary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この日記を閲覧する権限がありません");
        }

        return DiaryResponse.from(diary);
    }

    @Transactional
    public DiaryResponse updateDiary(Long diaryId, String username, DiaryUpdateRequest request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new ServiceException("404", "日記が見つかりません"));

        if (!diary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この日記を修正する権限がありません");
        }

        if (!diary.getDiaryDate().equals(request.diaryDate())) {
            diaryRepository.findByUserAndDiaryDate(diary.getUser(), request.diaryDate())
                    .ifPresent(d -> {
                        throw new ServiceException("400", "この日付の日記は既に存在します");
                    });
        }

        diary.update(request.title(), request.content(), request.diaryDate(), request.isPublic());

        return DiaryResponse.from(diary);
    }

    @Transactional
    public void deleteDiary(Long diaryId, String username) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new ServiceException("404", "日記が見つかりません"));

        if (!diary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この日記を削除する権限がありません");
        }

        diaryRepository.delete(diary);
    }
}
