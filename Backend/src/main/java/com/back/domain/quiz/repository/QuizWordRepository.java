package com.back.domain.quiz.repository;

import com.back.domain.quiz.entity.QuizWord;
import com.back.domain.quiz.entity.WordSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface    QuizWordRepository extends JpaRepository<QuizWord, Long> {

    // JLPT 레벨별 조회
    List<QuizWord> findBySourceAndSourceDetail(WordSource source, String sourceDetail);

    // JLPT 레벨별 랜덤 조회
    @Query(value = "SELECT * FROM quiz_words WHERE source = :source AND source_detail = :level ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuizWord> findRandomByJlptLevel(@Param("source") String source, @Param("level") String level, @Param("limit") int limit);

    // 특정 단어 제외하고 랜덤 조회 (오답 선택지용)
    @Query(value = "SELECT * FROM quiz_words WHERE source = :source AND source_detail = :level AND id != :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuizWord> findRandomByJlptLevelExcluding(@Param("source") String source, @Param("level") String level, @Param("excludeId") Long excludeId, @Param("limit") int limit);

    // 출처별 개수 조회
    long countBySourceAndSourceDetail(WordSource source, String sourceDetail);

    // 모든 JLPT 단어 조회
    List<QuizWord> findBySource(WordSource source);

    boolean existsBySourceAndSourceDetailAndWordAndHiragana(
            WordSource source,
            String sourceDetail,
            String word,
            String hiragana
    );

    long countBySource(WordSource source);
}
