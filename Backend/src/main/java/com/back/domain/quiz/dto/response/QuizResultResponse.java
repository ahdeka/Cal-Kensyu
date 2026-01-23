package com.back.domain.quiz.dto.response;

import lombok.Builder;

@Builder
public record QuizResultResponse(
        boolean isCorrect,      // Whether answer is correct
        String correctAnswer,   // Correct answer
        String explanation      // Explanation
) {
}