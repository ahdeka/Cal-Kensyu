package com.back.domain.vocabulary.dto.request;

import com.back.domain.vocabulary.entity.StudyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VocabularyUpdateRequest(
        @NotBlank(message = "Word is required")
        @Size(max = 100, message = "Word must be within 100 characters")
        String word,

        @NotBlank(message = "Hiragana is required")
        @Size(max = 100, message = "Hiragana must be within 100 characters")
        String hiragana,

        @NotBlank(message = "Meaning is required")
        @Size(max = 500, message = "Meaning must be within 500 characters")
        String meaning,

        @Size(max = 1000, message = "Example sentence must be within 1000 characters")
        String exampleSentence,

        @Size(max = 1000, message = "Example translation must be within 1000 characters")
        String exampleTranslation,

        @NotNull(message = "Study status is required")
        StudyStatus studyStatus
) {
}