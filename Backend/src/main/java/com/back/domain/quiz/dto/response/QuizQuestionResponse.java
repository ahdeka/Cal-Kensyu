package com.back.domain.quiz.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record QuizQuestionResponse(
        Long id,              // Word ID
        String question,      // Question ("食べる" or "たべる")
        String questionType,  // Question type description ("What is the reading of this word?")
        List<String> choices, // 4 choices (shuffled)
        String correctAnswer, // Correct answer
        String level,         // JLPT level ("N5", "N3")
        String explanation    // Explanation
) {
}