package com.back.domain.quiz.controller;

import com.back.domain.quiz.dto.response.QuizQuestionResponse;
import com.back.domain.quiz.entity.JlptLevel;
import com.back.domain.quiz.service.QuizService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz Practice API")
public class QuizController {

    private final QuizService quizService;

    private static final int MIN_QUIZ_COUNT = 1;
    private static final int MAX_QUIZ_COUNT = 50;

    @GetMapping("/{level}")
    @Operation(summary = "Generate JLPT level quiz", description = "Generates quiz for the specified JLPT level")
    public ResponseEntity<RsData<List<QuizQuestionResponse>>> generateQuiz(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String level,
            @RequestParam(defaultValue = "10") int count
    ) {

        JlptLevel jlptLevel = JlptLevel.fromString(level);
        validateQuizCount(count);

        List<QuizQuestionResponse> quizzes = quizService.generateJlptQuiz(jlptLevel, count);

        return ResponseEntity.ok(
                RsData.of("200", "Quiz generated successfully", quizzes)
        );
    }

    private void validateQuizCount(int count) {
        if (count < MIN_QUIZ_COUNT || count > MAX_QUIZ_COUNT) {
            throw new ServiceException("400",
                    String.format("Question count must be between %d and %d", MIN_QUIZ_COUNT, MAX_QUIZ_COUNT));
        }
    }
}
