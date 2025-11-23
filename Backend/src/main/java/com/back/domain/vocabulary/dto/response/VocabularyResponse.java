package com.back.domain.vocabulary.dto.response;

import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VocabularyResponse(
        Long id,
        String word,
        String hiragana,
        String meaning,
        String exampleSentence,
        String exampleTranslation,
        StudyStatus studyStatus,
        String studyStatusDisplay,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
    public static VocabularyResponse from(Vocabulary vocabulary) {
        return VocabularyResponse.builder()
                .id(vocabulary.getId())
                .word(vocabulary.getWord())
                .hiragana(vocabulary.getHiragana())
                .meaning(vocabulary.getMeaning())
                .exampleSentence(vocabulary.getExampleSentence())
                .exampleTranslation(vocabulary.getExampleTranslation())
                .studyStatus(vocabulary.getStudyStatus())
                .studyStatusDisplay(vocabulary.getStudyStatus().getDisplayName())
                .createDate(vocabulary.getCreateDate())
                .updateDate(vocabulary.getUpdateDate())
                .build();
    }
}