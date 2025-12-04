package com.back.domain.quiz.service;

import com.back.domain.quiz.dto.response.QuizQuestionResponse;
import com.back.domain.quiz.entity.QuizWord;
import com.back.domain.quiz.entity.WordSource;
import com.back.domain.quiz.repository.QuizWordRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizWordRepository quizWordRepository;

    // JLPT 레벨별 퀴즈 생성
    public List<QuizQuestionResponse> generateJlptQuiz(String level, int count) {
        // 1. 해당 레벨의 단어 개수 확인
        long wordCount = quizWordRepository.countBySourceAndSourceDetail(WordSource.JLPT, level);

        if (wordCount < count) {
            throw new ServiceException("400",
                    String.format("JLPT %s 単語が不足しています。必要: %d個、実際: %d個", level, count, wordCount));
        }

        if (wordCount < count + 3) {
            count = (int)(wordCount - 3);
            if (count < 1) {
                throw new ServiceException("400",
                        String.format("JLPT %s 単語が不足しています（最低 4個必要）", level));
            }
        }

        // 2. 랜덤으로 문제 단어 선택
        List<QuizWord> questionWords = quizWordRepository.findRandomByJlptLevel(
                WordSource.JLPT.name(), level, count
        );

        // 3. 각 단어마다 퀴즈 생성
        List<QuizQuestionResponse> quizQuestionResponses = new ArrayList<>();
        for (QuizWord correctWord : questionWords) {
            QuizQuestionResponse quiz = generateSingleQuiz(correctWord, level);
            quizQuestionResponses.add(quiz);
        }

        return quizQuestionResponses;
    }

    // 단일 퀴즈 생성
    private QuizQuestionResponse generateSingleQuiz(QuizWord correctWord, String level) {
        // 1. 오답 선택지 3개 가져오기 (같은 레벨에서)
        List<QuizWord> wrongWords = quizWordRepository.findRandomByJlptLevelExcluding(
                WordSource.JLPT.name(),
                level,
                correctWord.getId(),
                3
        );

        if (wrongWords.size() < 3) {
            throw new ServiceException("500", "오답 선택지 생성 실패");
        }

        // 2. 퀴즈 타입 랜덤 결정 (한자→히라가나 or 히라가나→뜻)
        QuizType quizType = Math.random() < 0.5 ? QuizType.KANJI_TO_HIRAGANA : QuizType.HIRAGANA_TO_MEANING;

        // 3. 문제와 선택지 생성
        String question;
        String questionType;
        List<String> choices = new ArrayList<>();
        String correctAnswer;

        if (quizType == QuizType.KANJI_TO_HIRAGANA) {
            // 한자 → 히라가나
            question = correctWord.getWord();
            questionType = "この単語の読み方は？";
            correctAnswer = correctWord.getHiragana();

            choices.add(correctWord.getHiragana());
            for (QuizWord wrong : wrongWords) {
                choices.add(wrong.getHiragana());
            }
        } else {
            // 히라가나 → 뜻
            question = correctWord.getHiragana();
            questionType = "この単語の意味は？";
            correctAnswer = correctWord.getMeaning();

            choices.add(correctWord.getMeaning());
            for (QuizWord wrong : wrongWords) {
                choices.add(wrong.getMeaning());
            }
        }

        // 4. 선택지 섞기
        Collections.shuffle(choices);

        // 5. DTO 생성
        return QuizQuestionResponse.builder()
                .id(correctWord.getId())
                .question(question)
                .questionType(questionType)
                .choices(choices)
                .correctAnswer(correctAnswer)
                .level(level)
                .explanation(String.format(
                        "「%s」は「%s」と読み、「%s」という意味です。",
                        correctWord.getWord(),
                        correctWord.getHiragana(),
                        correctWord.getMeaning()
                ))
                .build();
    }

    /**
     * 퀴즈 타입 Enum
     */
    private enum QuizType {
        KANJI_TO_HIRAGANA,  // 한자 → 히라가나
        HIRAGANA_TO_MEANING // 히라가나 → 뜻
    }
}
