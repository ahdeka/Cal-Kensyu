package com.back.domain.quiz.service;

import com.back.domain.quiz.dto.response.QuizQuestionResponse;
import com.back.domain.quiz.entity.JlptLevel;
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

    private static final int MINIMUM_WORDS_REQUIRED = 4;
    private static final int WRONG_ANSWERS_COUNT = 3;

    // JLPTレベル別クイズ生成
    public List<QuizQuestionResponse> generateJlptQuiz(JlptLevel jlptLevel, int count) {

        String levelName = jlptLevel.name();

        long wordCount = quizWordRepository.countBySourceAndSourceDetail(WordSource.JLPT, levelName);

        validateWordCount(wordCount, count, levelName);

        int actualCount = adjustQuizCount(wordCount, count);

        // ランダムに問題単語を選択
        List<QuizWord> questionWords = quizWordRepository.findRandomByJlptLevel(
                WordSource.JLPT.name(),
                levelName,
                actualCount
        );

        // 各単語ごとにクイズを生成
        List<QuizQuestionResponse> quizzes = questionWords.stream()
                .map(word -> generateSingleQuiz(word, levelName))
                .collect(Collectors.toList());

        return quizzes;
    }

    private void validateWordCount(long wordCount, int requestedCount, String level) {
        if (wordCount < MINIMUM_WORDS_REQUIRED) {
            throw new ServiceException("400",
                    String.format("JLPT %s の単語が不足しています（最低 %d個必要、実際: %d個）",
                            level, MINIMUM_WORDS_REQUIRED, wordCount));
        }

        if (wordCount < requestedCount) {
            log.warn("要求されたクイズ数が単語数を超えています - 要求: {}, 実際: {}", requestedCount, wordCount);
        }
    }

    private int adjustQuizCount(long wordCount, int requestedCount) {
        // 誤答選択肢のために最低3つの余裕が必要
        long maxPossibleCount = wordCount - WRONG_ANSWERS_COUNT;

        if (requestedCount > maxPossibleCount) {
            return (int) Math.max(1, maxPossibleCount);
        }

        return requestedCount;
    }

    // 単一クイズ生成
    private QuizQuestionResponse generateSingleQuiz(QuizWord correctWord, String level) {
        // クイズタイプをランダムに決定（漢字→ひらがな or ひらがな→意味）
        QuizType quizType = determineQuizType();

        // 2. 誤答選択肢3つを取得（似た長さの単語）
        List<QuizWord> wrongWords = getSimilarLengthWrongAnswers(correctWord, level, quizType);

        validateWrongAnswers(wrongWords);

        QuizContent content = buildQuizContent(correctWord, wrongWords, quizType, level);

        Collections.shuffle(content.choices);

        return QuizQuestionResponse.builder()
                .id(correctWord.getId())
                .question(content.question)
                .questionType(content.questionType)
                .choices(content.choices)
                .correctAnswer(content.correctAnswer)
                .level(level)
                .explanation(buildExplanation(correctWord))
                .build();
    }

    private QuizType determineQuizType() {
        return Math.random() < 0.5 ? QuizType.KANJI_TO_HIRAGANA : QuizType.HIRAGANA_TO_MEANING;
    }

    private void validateWrongAnswers(List<QuizWord> wrongWords) {
        if (wrongWords.size() < WRONG_ANSWERS_COUNT) {
            throw new ServiceException("500",
                    String.format("誤答選択肢の生成に失敗しました（必要: %d個、実際: %d個）",
                            WRONG_ANSWERS_COUNT, wrongWords.size()));
        }
    }

    // クイズの内容（問題、選択肢、正解）を構築
    private QuizContent buildQuizContent(QuizWord correctWord, List<QuizWord> wrongWords, QuizType quizType, String level) {
        QuizContent content = new QuizContent();
        content.choices = new ArrayList<>();

        if (quizType == QuizType.KANJI_TO_HIRAGANA) {
            // 漢字 → ひらがな
            content.question = correctWord.getWord();
            content.questionType = "この単語の読み方は？";
            content.correctAnswer = correctWord.getHiragana();

            content.choices.add(correctWord.getHiragana());
            wrongWords.forEach(wrong -> content.choices.add(wrong.getHiragana()));
        } else {
            // 漢字 → 意味（レベル別に表示方式を変更）
            JlptLevel currentLevel = JlptLevel.valueOf(level);

            if (currentLevel == JlptLevel.N5 || currentLevel == JlptLevel.N4) {
                content.question = String.format("%s（%s）",
                        correctWord.getWord(),
                        correctWord.getHiragana());
            } else {
                content.question = correctWord.getWord();
            }

            content.questionType = "この単語の意味は？";
            content.correctAnswer = correctWord.getMeaning();

            content.choices.add(correctWord.getMeaning());
            wrongWords.forEach(wrong -> content.choices.add(wrong.getMeaning()));
        }

        return content;
    }

    // 解説文を生成
    private String buildExplanation(QuizWord word) {
        return String.format(
                "「%s」は「%s」と読み、「%s」という意味です。",
                word.getWord(),
                word.getHiragana(),
                word.getMeaning()
        );
    }

    /**
     * 似た長さの誤答選択肢を取得
     * 正解と±2文字以内の単語を優先選択して難易度を高める
     */
    private List<QuizWord> getSimilarLengthWrongAnswers(QuizWord correctWord, String level, QuizType quizType) {
        // 正解の長さを計算
        int targetLength = calculateTargetLength(correctWord, quizType);

        // 同じレベルの全単語を取得（正解を除く）
        List<QuizWord> allWords = quizWordRepository.findBySourceAndSourceDetail(WordSource.JLPT, level)
                .stream()
                .filter(word -> !word.getId().equals(correctWord.getId()))
                .collect(Collectors.toList());

        // 優先順位別に単語を分類
        WordsByLength wordsByLength = categorizeWordsByLength(allWords, targetLength, quizType);

        // 優先順位に従って誤答を選択（3つ必要）
        return selectWrongAnswers(wordsByLength);
    }

    /**
     * 対象の長さを計算
     *
     * @param word 単語
     * @param quizType クイズタイプ
     * @return 文字数
     */
    private int calculateTargetLength(QuizWord word, QuizType quizType) {
        return quizType == QuizType.KANJI_TO_HIRAGANA
                ? word.getHiragana().length()
                : word.getMeaning().length();
    }

    /**
     * 単語を長さの差で分類
     *
     * @param words 単語リスト
     * @param targetLength 目標の長さ
     * @param quizType クイズタイプ
     * @return 分類された単語
     */
    private WordsByLength categorizeWordsByLength(List<QuizWord> words, int targetLength, QuizType quizType) {
        WordsByLength result = new WordsByLength();

        for (QuizWord word : words) {
            int wordLength = calculateTargetLength(word, quizType);
            int diff = Math.abs(wordLength - targetLength);

            if (diff == 0) {
                result.exactMatch.add(word);
            } else if (diff == 1) {
                result.closeMatch.add(word);
            } else if (diff == 2) {
                result.nearMatch.add(word);
            } else {
                result.others.add(word);
            }
        }

        // 各グループをシャッフル
        Collections.shuffle(result.exactMatch);
        Collections.shuffle(result.closeMatch);
        Collections.shuffle(result.nearMatch);
        Collections.shuffle(result.others);

        return result;
    }

    /**
     * 優先順位に従って誤答を選択
     *
     * @param wordsByLength 長さ別に分類された単語
     * @return 選択された誤答（最大3つ）
     */
    private List<QuizWord> selectWrongAnswers(WordsByLength wordsByLength) {
        List<QuizWord> wrongAnswers = new ArrayList<>();

        // 第1優先順位: 正確に同じ長さ
        addWordsUpToLimit(wrongAnswers, wordsByLength.exactMatch, WRONG_ANSWERS_COUNT);
        if (wrongAnswers.size() >= WRONG_ANSWERS_COUNT) {
            return wrongAnswers.subList(0, WRONG_ANSWERS_COUNT);
        }

        // 第2優先順位: ±1文字
        addWordsUpToLimit(wrongAnswers, wordsByLength.closeMatch, WRONG_ANSWERS_COUNT);
        if (wrongAnswers.size() >= WRONG_ANSWERS_COUNT) {
            return wrongAnswers.subList(0, WRONG_ANSWERS_COUNT);
        }

        // 第3優先順位: ±2文字
        addWordsUpToLimit(wrongAnswers, wordsByLength.nearMatch, WRONG_ANSWERS_COUNT);
        if (wrongAnswers.size() >= WRONG_ANSWERS_COUNT) {
            return wrongAnswers.subList(0, WRONG_ANSWERS_COUNT);
        }

        // 第4優先順位: 残り（不足する場合はここで補充）
        addWordsUpToLimit(wrongAnswers, wordsByLength.others, WRONG_ANSWERS_COUNT);

        return wrongAnswers;
    }

    /**
     * リストから指定数まで単語を追加
     *
     * @param target 追加先リスト
     * @param source 追加元リスト
     * @param limit 最大数
     */
    private void addWordsUpToLimit(List<QuizWord> target, List<QuizWord> source, int limit) {
        int remaining = limit - target.size();
        target.addAll(source.stream().limit(remaining).collect(Collectors.toList()));
    }

    /**
     * クイズタイプ Enum
     */
    private enum QuizType {
        KANJI_TO_HIRAGANA,  // 漢字 → ひらがな
        HIRAGANA_TO_MEANING // ひらがな → 意味
    }

    /**
     * クイズの内容を保持する内部クラス
     */
    private static class QuizContent {
        String question;
        String questionType;
        String correctAnswer;
        List<String> choices;
    }

    /**
     * 長さ別に分類された単語を保持する内部クラス
     */
    private static class WordsByLength {
        List<QuizWord> exactMatch = new ArrayList<>();  // 正確に同じ長さ
        List<QuizWord> closeMatch = new ArrayList<>();  // ±1文字
        List<QuizWord> nearMatch = new ArrayList<>();   // ±2文字
        List<QuizWord> others = new ArrayList<>();      // その他
    }
}