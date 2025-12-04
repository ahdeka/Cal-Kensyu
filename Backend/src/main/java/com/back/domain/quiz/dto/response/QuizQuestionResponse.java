package com.back.domain.quiz.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record QuizQuestionResponse(
        Long id,              // 단어 ID
        String question,      // 문제 ("食べる" or "たべる")
        String questionType,  // 문제 타입 설명 ("この単語の読み方は？")
        List<String> choices, // 선택지 4개 (섞인 상태)
        String correctAnswer, // 정답
        String level,         // JLPT 레벨 ("N5", "N3")
        String explanation    // 해설
) {
}