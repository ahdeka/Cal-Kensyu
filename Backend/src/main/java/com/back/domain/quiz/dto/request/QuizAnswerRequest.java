package com.back.domain.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizAnswerRequest(
        @NotNull(message = "Question ID is required")
        Long questionId,

        @NotBlank(message = "Answer is required")
        String answer
) {
}