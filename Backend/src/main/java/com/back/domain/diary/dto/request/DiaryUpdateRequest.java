package com.back.domain.diary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DiaryUpdateRequest(
        @NotNull(message = "日付は必須です")
        LocalDate diaryDate,

        @NotBlank(message = "タイトルは必須です")
        @Size(max = 100, message = "タイトルは100文字以内で入力してください")
        String title,

        @NotBlank(message = "内容は必須です")
        String content,

        @NotNull(message = "公開設定は必須です")
        Boolean isPublic
) {
}