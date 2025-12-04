package com.back.domain.quiz.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "quiz_words")
public class QuizWord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WordSource source;

    @Column(length = 20)
    private String sourceDetail;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(nullable = false, length = 100)
    private String hiragana;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(columnDefinition = "TEXT")
    private String exampleTranslation;


    // ===== Helper Method ===== //
    public boolean isJlptWord() {
        return source == WordSource.JLPT;
    }

    public boolean isUserWord() {
        return source == WordSource.USER_VOCABULARY || source == WordSource.USER_DIARY;
    }

    public JlptLevel getJlptLevel() {
        if (source == WordSource.JLPT && sourceDetail != null) {
            return JlptLevel.fromString(sourceDetail);
        }
        return null;
    }

    public boolean isJlptLevel(JlptLevel level) {
        return isJlptWord() && level.name().equals(sourceDetail);
    }

}
