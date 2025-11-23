package com.back.domain.vocabulary.dto.response;

import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VocabularyListResponse(
        Long id,
        String word,
        String hiragana,
        String meaning,
        StudyStatus studyStatus,
        String studyStatusDisplay,
        LocalDateTime createDate
) {
    public static VocabularyListResponse from(Vocabulary vocabulary) {
        return VocabularyListResponse.builder()
                .id(vocabulary.getId())
                .word(vocabulary.getWord())
                .hiragana(vocabulary.getHiragana())
                .meaning(vocabulary.getMeaning())
                .studyStatus(vocabulary.getStudyStatus())
                .studyStatusDisplay(vocabulary.getStudyStatus().getDisplayName())
                .createDate(vocabulary.getCreateDate())
                .build();
    }
}