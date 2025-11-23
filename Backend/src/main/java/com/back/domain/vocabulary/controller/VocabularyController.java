package com.back.domain.vocabulary.controller;

import com.back.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.back.domain.vocabulary.dto.request.VocabularyUpdateRequest;
import com.back.domain.vocabulary.dto.response.VocabularyListResponse;
import com.back.domain.vocabulary.dto.response.VocabularyResponse;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.service.VocabularyService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vocabulary", description = "単語帳 API")
@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @Operation(summary = "単語登録", description = "新しい単語を単語帳に登録します")
    @PostMapping
    public ResponseEntity<RsData<VocabularyResponse>> createVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VocabularyCreateRequest request) {

        VocabularyResponse response = vocabularyService.createVocabulary(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                RsData.of("200", "単語を登録しました", response)
        );
    }

    @Operation(summary = "全単語取得", description = "自分の単語帳の全ての単語を取得します")
    @GetMapping
    public ResponseEntity<RsData<List<VocabularyListResponse>>> getMyVocabularies(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<VocabularyListResponse> response = vocabularyService.getMyVocabularies(userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "単語一覧を取得しました", response)
        );
    }

    @Operation(summary = "暗記状態別単語取得", description = "暗記状態でフィルタリングして単語を取得します")
    @GetMapping("/status/{studyStatus}")
    public ResponseEntity<RsData<List<VocabularyListResponse>>> getVocabulariesByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable StudyStatus studyStatus) {

        List<VocabularyListResponse> response = vocabularyService
                .getVocabulariesByStatus(userDetails.getUsername(), studyStatus);

        return ResponseEntity.ok(
                RsData.of("200", "単語一覧を取得しました", response)
        );
    }

    @Operation(summary = "単語検索", description = "単語名でキーワード検索します")
    @GetMapping("/search")
    public ResponseEntity<RsData<List<VocabularyListResponse>>> searchVocabularies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {

        List<VocabularyListResponse> response = vocabularyService
                .searchVocabularies(userDetails.getUsername(), keyword);

        return ResponseEntity.ok(
                RsData.of("200", "検索結果を取得しました", response)
        );
    }

    @Operation(summary = "単語詳細取得", description = "単語の詳細情報を取得します")
    @GetMapping("/{vocabularyId}")
    public ResponseEntity<RsData<VocabularyResponse>> getVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId) {

        VocabularyResponse response = vocabularyService.getVocabulary(vocabularyId, userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "単語詳細を取得しました", response)
        );
    }

    @Operation(summary = "単語修正", description = "単語の情報を修正します")
    @PutMapping("/{vocabularyId}")
    public ResponseEntity<RsData<VocabularyResponse>> updateVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId,
            @Valid @RequestBody VocabularyUpdateRequest request) {

        VocabularyResponse response = vocabularyService
                .updateVocabulary(vocabularyId, userDetails.getUsername(), request);

        return ResponseEntity.ok(
                RsData.of("200", "単語を修正しました", response)
        );
    }

    @Operation(summary = "単語削除", description = "単語を削除します")
    @DeleteMapping("/{vocabularyId}")
    public ResponseEntity<RsData<Void>> deleteVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId) {

        vocabularyService.deleteVocabulary(vocabularyId, userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "単語を削除しました", null)
        );
    }

    @Operation(summary = "学習状態変更", description = "単語の学習状態だけを変更します")
    @PatchMapping("/{vocabularyId}/status")
    public ResponseEntity<RsData<VocabularyResponse>> updateStudyStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId,
            @RequestParam StudyStatus studyStatus) {

        VocabularyResponse response = vocabularyService
                .updateStudyStatus(vocabularyId, userDetails.getUsername(), studyStatus);

        return ResponseEntity.ok(
                RsData.of("200", "学習状態を変更しました", response)
        );
    }
}
