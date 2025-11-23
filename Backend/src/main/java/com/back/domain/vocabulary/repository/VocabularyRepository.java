package com.back.domain.vocabulary.repository;

import com.back.domain.user.entity.User;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    List<Vocabulary> findByUserOrderByCreateDateDesc(User user);

    List<Vocabulary> findByUserAndStudyStatusOrderByCreateDateDesc(User user, StudyStatus studyStatus);

    @Query("SELECT v FROM Vocabulary v WHERE v.user = :user " +
            "AND (v.word LIKE %:keyword% " +
            "OR v.hiragana LIKE %:keyword% " +
            "OR v.meaning LIKE %:keyword%) " +
            "ORDER BY v.createDate DESC")
    List<Vocabulary> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);

    boolean existsByIdAndUser(Long id, User user);
}
