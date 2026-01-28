package com.back.domain.quiz.repository;

import com.back.domain.quiz.entity.QuizWord;
import com.back.domain.quiz.entity.WordSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizWordRepository extends JpaRepository<QuizWord, Long> {

    // Query by JLPT level
    List<QuizWord> findBySourceAndSourceDetail(WordSource source, String sourceDetail);

    // Random query by JLPT level
    @Query(value = "SELECT * FROM quiz_words WHERE source = :source AND source_detail = :level ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuizWord> findRandomByJlptLevel(@Param("source") String source, @Param("level") String level, @Param("limit") int limit);

    // Random query excluding specific word (for incorrect answer options)
    @Query(value = "SELECT * FROM quiz_words WHERE source = :source AND source_detail = :level AND id != :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuizWord> findRandomByJlptLevelExcluding(@Param("source") String source, @Param("level") String level, @Param("excludeId") Long excludeId, @Param("limit") int limit);

    // Count by source
    long countBySourceAndSourceDetail(WordSource source, String sourceDetail);

    // Query all JLPT words
    List<QuizWord> findBySource(WordSource source);

    boolean existsBySourceAndSourceDetailAndWordAndHiragana(
            WordSource source,
            String sourceDetail,
            String word,
            String hiragana
    );

    long countBySource(WordSource source);
}