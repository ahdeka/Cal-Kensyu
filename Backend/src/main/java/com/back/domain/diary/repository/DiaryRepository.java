package com.back.domain.diary.repository;

import com.back.domain.diary.entity.Diary;
import com.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findByUserOrderByCreateDateDesc(User user);

    List<Diary> findByIsPublicTrueOrderByCreateDateDesc();

    Optional<Diary> findByUserAndDiaryDate(User user, LocalDate diaryDate);

    boolean existsByIdAndUser(Long id, User user);
}
