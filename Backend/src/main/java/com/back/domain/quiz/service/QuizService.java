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

    /**
     * Generate JLPT level quiz
     */
    public List<QuizQuestionResponse> generateJlptQuiz(JlptLevel jlptLevel, int count) {

        String levelName = jlptLevel.name();

        long wordCount = quizWordRepository.countBySourceAndSourceDetail(WordSource.JLPT, levelName);

        validateWordCount(wordCount, count, levelName);

        int actualCount = adjustQuizCount(wordCount, count);

        // Randomly select question words
        List<QuizWord> questionWords = quizWordRepository.findRandomByJlptLevel(
                WordSource.JLPT.name(),
                levelName,
                actualCount
        );

        // Generate quiz for each word
        List<QuizQuestionResponse> quizzes = questionWords.stream()
                .map(word -> generateSingleQuiz(word, levelName))
                .collect(Collectors.toList());

        return quizzes;
    }

    private void validateWordCount(long wordCount, int requestedCount, String level) {
        if (wordCount < MINIMUM_WORDS_REQUIRED) {
            throw new ServiceException("400",
                    String.format("Insufficient words for JLPT %s (minimum required: %d, actual: %d)",
                            level, MINIMUM_WORDS_REQUIRED, wordCount));
        }

        if (wordCount < requestedCount) {
            log.warn("Requested quiz count exceeds available words - requested: {}, actual: {}", requestedCount, wordCount);
        }
    }

    private int adjustQuizCount(long wordCount, int requestedCount) {
        // Need at least 3 extra words for wrong answer choices
        long maxPossibleCount = wordCount - WRONG_ANSWERS_COUNT;

        if (requestedCount > maxPossibleCount) {
            return (int) Math.max(1, maxPossibleCount);
        }

        return requestedCount;
    }

    /**
     * Generate single quiz
     */
    private QuizQuestionResponse generateSingleQuiz(QuizWord correctWord, String level) {
        // Randomly determine quiz type (kanji→hiragana or hiragana→meaning)
        QuizType quizType = determineQuizType();

        // Get 3 wrong answer choices (words with similar length)
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
                    String.format("Failed to generate wrong answer choices (required: %d, actual: %d)",
                            WRONG_ANSWERS_COUNT, wrongWords.size()));
        }
    }

    /**
     * Build quiz content (question, choices, correct answer)
     */
    private QuizContent buildQuizContent(QuizWord correctWord, List<QuizWord> wrongWords, QuizType quizType, String level) {
        QuizContent content = new QuizContent();
        content.choices = new ArrayList<>();

        if (quizType == QuizType.KANJI_TO_HIRAGANA) {
            // Kanji → Hiragana
            content.question = correctWord.getWord();
            content.questionType = "What is the reading of this word?";
            content.correctAnswer = correctWord.getHiragana();

            content.choices.add(correctWord.getHiragana());
            wrongWords.forEach(wrong -> content.choices.add(wrong.getHiragana()));
        } else {
            // Kanji → Meaning (display format changes by level)
            JlptLevel currentLevel = JlptLevel.valueOf(level);

            if (currentLevel == JlptLevel.N5 || currentLevel == JlptLevel.N4) {
                content.question = String.format("%s（%s）",
                        correctWord.getWord(),
                        correctWord.getHiragana());
            } else {
                content.question = correctWord.getWord();
            }

            content.questionType = "What is the meaning of this word?";
            content.correctAnswer = correctWord.getMeaning();

            content.choices.add(correctWord.getMeaning());
            wrongWords.forEach(wrong -> content.choices.add(wrong.getMeaning()));
        }

        return content;
    }

    /**
     * Generate explanation text
     */
    private String buildExplanation(QuizWord word) {
        return String.format(
                "「%s」is read as「%s」and means「%s」.",
                word.getWord(),
                word.getHiragana(),
                word.getMeaning()
        );
    }

    /**
     * Get wrong answer choices with similar length
     * Prioritize words within ±2 characters for increased difficulty
     */
    private List<QuizWord> getSimilarLengthWrongAnswers(QuizWord correctWord, String level, QuizType quizType) {
        // Calculate length of correct answer
        int targetLength = calculateTargetLength(correctWord, quizType);

        // Get all words of same level (excluding correct answer)
        List<QuizWord> allWords = quizWordRepository.findBySourceAndSourceDetail(WordSource.JLPT, level)
                .stream()
                .filter(word -> !word.getId().equals(correctWord.getId()))
                .collect(Collectors.toList());

        // Categorize words by priority
        WordsByLength wordsByLength = categorizeWordsByLength(allWords, targetLength, quizType);

        // Select wrong answers according to priority (need 3)
        return selectWrongAnswers(wordsByLength);
    }

    /**
     * Calculate target length
     *
     * @param word Word
     * @param quizType Quiz type
     * @return Character count
     */
    private int calculateTargetLength(QuizWord word, QuizType quizType) {
        return quizType == QuizType.KANJI_TO_HIRAGANA
                ? word.getHiragana().length()
                : word.getMeaning().length();
    }

    /**
     * Categorize words by length difference
     *
     * @param words Word list
     * @param targetLength Target length
     * @param quizType Quiz type
     * @return Categorized words
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

        // Shuffle each group
        Collections.shuffle(result.exactMatch);
        Collections.shuffle(result.closeMatch);
        Collections.shuffle(result.nearMatch);
        Collections.shuffle(result.others);

        return result;
    }

    /**
     * Select wrong answers according to priority
     *
     * @param wordsByLength Words categorized by length
     * @return Selected wrong answers (max 3)
     */
    private List<QuizWord> selectWrongAnswers(WordsByLength wordsByLength) {
        List<QuizWord> wrongAnswers = new ArrayList<>();

        // 1st priority: Exact same length
        addWordsUpToLimit(wrongAnswers, wordsByLength.exactMatch, WRONG_ANSWERS_COUNT);
        if (wrongAnswers.size() >= WRONG_ANSWERS_COUNT) {
            return wrongAnswers.subList(0, WRONG_ANSWERS_COUNT);
        }

        // 2nd priority: ±1 character
        addWordsUpToLimit(wrongAnswers, wordsByLength.closeMatch, WRONG_ANSWERS_COUNT);
        if (wrongAnswers.size() >= WRONG_ANSWERS_COUNT) {
            return wrongAnswers.subList(0, WRONG_ANSWERS_COUNT);
        }

        // 3rd priority: ±2 characters
        addWordsUpToLimit(wrongAnswers, wordsByLength.nearMatch, WRONG_ANSWERS_COUNT);
        if (wrongAnswers.size() >= WRONG_ANSWERS_COUNT) {
            return wrongAnswers.subList(0, WRONG_ANSWERS_COUNT);
        }

        // 4th priority: Rest (fill in if insufficient)
        addWordsUpToLimit(wrongAnswers, wordsByLength.others, WRONG_ANSWERS_COUNT);

        return wrongAnswers;
    }

    /**
     * Add words from list up to specified limit
     *
     * @param target Target list
     * @param source Source list
     * @param limit Maximum count
     */
    private void addWordsUpToLimit(List<QuizWord> target, List<QuizWord> source, int limit) {
        int remaining = limit - target.size();
        target.addAll(source.stream().limit(remaining).collect(Collectors.toList()));
    }

    /**
     * Quiz type Enum
     */
    private enum QuizType {
        KANJI_TO_HIRAGANA,  // Kanji → Hiragana
        HIRAGANA_TO_MEANING // Hiragana → Meaning
    }

    /**
     * Inner class to hold quiz content
     */
    private static class QuizContent {
        String question;
        String questionType;
        String correctAnswer;
        List<String> choices;
    }

    /**
     * Inner class to hold words categorized by length
     */
    private static class WordsByLength {
        List<QuizWord> exactMatch = new ArrayList<>();  // Exact same length
        List<QuizWord> closeMatch = new ArrayList<>();  // ±1 character
        List<QuizWord> nearMatch = new ArrayList<>();   // ±2 characters
        List<QuizWord> others = new ArrayList<>();      // Others
    }
}