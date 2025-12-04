package com.back.domain.quiz.dto.response;

import lombok.Builder;

@Builder
public record QuizResultResponse(
        boolean isCorrect,      // 정답 여부
        String correctAnswer,   // 정답
        String explanation      // 해설
) {
}