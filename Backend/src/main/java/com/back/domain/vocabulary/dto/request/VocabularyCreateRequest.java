package com.back.domain.vocabulary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VocabularyCreateRequest(
        @NotBlank(message = "単語を入力してください")
        @Size(max = 100, message = "単語は100文字以内で入力してください")
        String word,

        @NotBlank(message = "ひらがなを入力してください")
        @Size(max = 100, message = "ひらがなは100文字以内で入力してください")
        String hiragana,

        @NotBlank(message = "意味を入力してください")
        @Size(max = 500, message = "意味は500文字以内で入力してください")
        String meaning,

        @Size(max = 1000, message = "例文は1000文字以内で入力してください")
        String exampleSentence,

        @Size(max = 1000, message = "例文翻訳は1000文字以内で入力してください")
        String exampleTranslation
) {
}