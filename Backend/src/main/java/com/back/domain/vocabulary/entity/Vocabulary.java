package com.back.domain.vocabulary.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "vocabularies")
public class Vocabulary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudyStatus studyStatus = StudyStatus.NOT_STUDIED;


    // ===== Helper Method ===== //
    public void update(String word, String hiragana, String meaning,
                       String exampleSentence, String exampleTranslation,
                       StudyStatus studyStatus) {
        this.word = word;
        this.hiragana = hiragana;
        this.meaning = meaning;
        this.exampleSentence = exampleSentence;
        this.exampleTranslation = exampleTranslation;
        this.studyStatus = studyStatus;
    }

    public void updateStudyStatus(StudyStatus studyStatus) {
        this.studyStatus = studyStatus;
    }
}
