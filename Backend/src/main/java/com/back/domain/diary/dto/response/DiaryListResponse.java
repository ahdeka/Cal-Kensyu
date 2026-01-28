package com.back.domain.diary.dto.response;

import com.back.domain.diary.entity.Diary;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record DiaryListResponse(
        Long id,
        String nickname,
        LocalDate diaryDate, // Date of diary content
        String title,
        String contentPreview,
        boolean isPublic,
        LocalDateTime createDate // Date created
) {
    public static DiaryListResponse from(Diary diary) {
        return DiaryListResponse.builder()
                .id(diary.getId())
                .nickname(diary.getUser().getNickname())
                .diaryDate(diary.getDiaryDate())
                .title(diary.getTitle())
                .contentPreview(diary.getContentPreview())  // Use Entity's method
                .isPublic(diary.isPublic())
                .createDate(diary.getCreateDate())
                .build();
    }
}