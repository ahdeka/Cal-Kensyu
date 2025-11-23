package com.back.domain.diary.dto.response;

import com.back.domain.diary.entity.Diary;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record DiaryResponse(
        Long id,
        String username,
        String nickname,
        LocalDate diaryDate,
        String title,
        String content,
        boolean isPublic,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .username(diary.getUser().getUsername())
                .nickname(diary.getUser().getNickname())
                .diaryDate(diary.getDiaryDate())
                .title(diary.getTitle())
                .content(diary.getContent())
                .isPublic(diary.isPublic())
                .createDate(diary.getCreateDate())
                .updateDate(diary.getUpdateDate())
                .build();
    }
}