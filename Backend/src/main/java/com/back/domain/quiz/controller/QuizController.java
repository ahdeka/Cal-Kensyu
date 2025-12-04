package com.back.domain.quiz.controller;

import com.back.domain.quiz.dto.response.QuizQuestionResponse;
import com.back.domain.quiz.service.QuizService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "問題演習 API")
public class QuizController {

    private final QuizService quizService;


    @GetMapping("/{level}")
    @Operation(summary = "JLPT レベル別クイズ生成", description = "指定されたJLPTレベルのクイズを生成します")
    public RsData<List<QuizQuestionResponse>> generateQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String level,
            @RequestParam(defaultValue = "10") int count
    ) {
        log.info("Quiz generation requested - level: {}, count: {}", level, count);

        if (userDetails == null) {
            return RsData.of("401", "ログインが必要です", null);
        }

        // 레벨 유효성 검사
        if (!isValidLevel(level)) {
            return RsData.of("400", "無効なレベルです。N5, N4, N3, N2, N1を指定してください", null);
        }

        // 문제 개수 유효성 검사
        if (count < 1 || count > 50) {
            return RsData.of("400", "問題数は1から50の間で指定してください", null);
        }

        try {
            List<QuizQuestionResponse> quizzes = quizService.generateJlptQuiz(level, count);
            return RsData.of("200", "クイズを生成しました", quizzes);
        } catch (Exception e) {
            log.error("Quiz generation failed", e);
            return RsData.of("500", e.getMessage(), null);
        }
    }

    private boolean isValidLevel(String level) {
        return level.matches("N[1-5]");
    }
}
