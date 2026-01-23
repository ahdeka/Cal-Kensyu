package com.back.domain.diary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DiaryUpdateRequest(
        @NotNull(message = "Date is required")
        LocalDate diaryDate,

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be within 100 characters")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        @NotNull(message = "Public setting is required")
        Boolean isPublic
) {
}