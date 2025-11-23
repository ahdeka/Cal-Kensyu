package com.back.domain.diary.dto.response;

import com.back.domain.diary.entity.Diary;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record DiaryListResponse(
    Long id,
    String nickname,
    LocalDate diaryDate, // 日記内容の日
    String title,
    String contentPreview,
    boolean isPublic,
    LocalDateTime createDate // 作成した日
) {
    public static DiaryListResponse from(Diary diary) {
        String preview = diary.getContent().length() > 100
                ? diary.getContent().substring(0, 100) + "..."
                : diary.getContent();

        return DiaryListResponse.builder()
                .id(diary.getId())
                .nickname(diary.getUser().getNickname())
                .diaryDate(diary.getDiaryDate())
                .title(diary.getTitle())
                .contentPreview(preview)
                .isPublic(diary.isPublic())
                .createDate(diary.getCreateDate())
                .build();
    }
}
