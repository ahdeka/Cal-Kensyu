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
import java.util.stream.Collectors;

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
        // 1. 퀴즈 타입 랜덤 결정 (한자→히라가나 or 히라가나→뜻)
        QuizType quizType = Math.random() < 0.5 ? QuizType.KANJI_TO_HIRAGANA : QuizType.HIRAGANA_TO_MEANING;

        // 2. 오답 선택지 3개 가져오기 (비슷한 길이의 단어)
        List<QuizWord> wrongWords = getSimilarLengthWrongAnswers(correctWord, level, quizType);

        if (wrongWords.size() < 3) {
            throw new ServiceException("500", "오답 선택지 생성 실패");
        }

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
     * 비슷한 길이의 오답 선택지를 가져옵니다.
     * 정답과 ±2 글자 이내의 단어를 우선 선택하여 난이도를 높입니다.
     */
    private List<QuizWord> getSimilarLengthWrongAnswers(QuizWord correctWord, String level, QuizType quizType) {
        // 정답의 길이 계산
        int targetLength = quizType == QuizType.KANJI_TO_HIRAGANA
                ? correctWord.getHiragana().length()
                : correctWord.getMeaning().length();

        // 같은 레벨의 모든 단어 가져오기 (정답 제외)
        List<QuizWord> allWords = quizWordRepository.findBySourceAndSourceDetail(WordSource.JLPT, level)
                .stream()
                .filter(word -> !word.getId().equals(correctWord.getId()))
                .collect(Collectors.toList());

        // 우선순위별로 단어 분류
        List<QuizWord> exactMatch = new ArrayList<>();      // 정확히 같은 길이
        List<QuizWord> closeMatch = new ArrayList<>();      // ±1 글자
        List<QuizWord> nearMatch = new ArrayList<>();       // ±2 글자
        List<QuizWord> others = new ArrayList<>();          // 그 외

        for (QuizWord word : allWords) {
            int wordLength = quizType == QuizType.KANJI_TO_HIRAGANA
                    ? word.getHiragana().length()
                    : word.getMeaning().length();

            int diff = Math.abs(wordLength - targetLength);

            if (diff == 0) {
                exactMatch.add(word);
            } else if (diff == 1) {
                closeMatch.add(word);
            } else if (diff == 2) {
                nearMatch.add(word);
            } else {
                others.add(word);
            }
        }

        // 각 그룹을 섞기
        Collections.shuffle(exactMatch);
        Collections.shuffle(closeMatch);
        Collections.shuffle(nearMatch);
        Collections.shuffle(others);

        // 우선순위에 따라 오답 선택 (3개 필요)
        List<QuizWord> wrongAnswers = new ArrayList<>();

        // 1순위: 정확히 같은 길이
        wrongAnswers.addAll(exactMatch.stream().limit(3).collect(Collectors.toList()));
        if (wrongAnswers.size() >= 3) {
            return wrongAnswers.subList(0, 3);
        }

        // 2순위: ±1 글자
        wrongAnswers.addAll(closeMatch.stream().limit(3 - wrongAnswers.size()).collect(Collectors.toList()));
        if (wrongAnswers.size() >= 3) {
            return wrongAnswers.subList(0, 3);
        }

        // 3순위: ±2 글자
        wrongAnswers.addAll(nearMatch.stream().limit(3 - wrongAnswers.size()).collect(Collectors.toList()));
        if (wrongAnswers.size() >= 3) {
            return wrongAnswers.subList(0, 3);
        }

        // 4순위: 나머지 (충분하지 않으면 여기서 채움)
        wrongAnswers.addAll(others.stream().limit(3 - wrongAnswers.size()).collect(Collectors.toList()));

        return wrongAnswers;
    }

    /**
     * 퀴즈 타입 Enum
     */
    private enum QuizType {
        KANJI_TO_HIRAGANA,  // 한자 → 히라가나
        HIRAGANA_TO_MEANING // 히라가나 → 뜻
    }
}