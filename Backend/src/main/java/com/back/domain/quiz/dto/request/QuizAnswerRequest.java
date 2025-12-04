package com.back.domain.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizAnswerRequest(
        @NotNull(message = "問題IDを入力してください")
        Long questionId,

        @NotBlank(message = "回答を入力してください")
        String answer
) {
}