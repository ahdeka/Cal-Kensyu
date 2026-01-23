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

@Tag(name = "Vocabulary", description = "Vocabulary API")
@RestController
@RequestMapping("/api/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @Operation(summary = "Register vocabulary", description = "Registers a new word to the vocabulary list")
    @PostMapping
    public ResponseEntity<RsData<VocabularyResponse>> createVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VocabularyCreateRequest request) {

        VocabularyResponse response = vocabularyService.createVocabulary(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                RsData.of("200", "Vocabulary registered successfully", response)
        );
    }

    @Operation(summary = "Get all vocabularies", description = "Retrieves all vocabulary words from user's list")
    @GetMapping
    public ResponseEntity<RsData<List<VocabularyListResponse>>> getMyVocabularies(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<VocabularyListResponse> response = vocabularyService.getMyVocabularies(userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "Vocabulary list retrieved successfully", response)
        );
    }

    @Operation(summary = "Get vocabularies by study status", description = "Retrieves vocabulary words filtered by study status")
    @GetMapping("/status/{studyStatus}")
    public ResponseEntity<RsData<List<VocabularyListResponse>>> getVocabulariesByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable StudyStatus studyStatus) {

        List<VocabularyListResponse> response = vocabularyService
                .getVocabulariesByStatus(userDetails.getUsername(), studyStatus);

        return ResponseEntity.ok(
                RsData.of("200", "Vocabulary list retrieved successfully", response)
        );
    }

    @Operation(summary = "Search vocabularies", description = "Searches vocabulary words by keyword")
    @GetMapping("/search")
    public ResponseEntity<RsData<List<VocabularyListResponse>>> searchVocabularies(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {

        List<VocabularyListResponse> response = vocabularyService
                .searchVocabularies(userDetails.getUsername(), keyword);

        return ResponseEntity.ok(
                RsData.of("200", "Search results retrieved successfully", response)
        );
    }

    @Operation(summary = "Get vocabulary details", description = "Retrieves detailed information of a vocabulary word")
    @GetMapping("/{vocabularyId}")
    public ResponseEntity<RsData<VocabularyResponse>> getVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId) {

        VocabularyResponse response = vocabularyService.getVocabulary(vocabularyId, userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "Vocabulary details retrieved successfully", response)
        );
    }

    @Operation(summary = "Update vocabulary", description = "Updates vocabulary word information")
    @PutMapping("/{vocabularyId}")
    public ResponseEntity<RsData<VocabularyResponse>> updateVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId,
            @Valid @RequestBody VocabularyUpdateRequest request) {

        VocabularyResponse response = vocabularyService
                .updateVocabulary(vocabularyId, userDetails.getUsername(), request);

        return ResponseEntity.ok(
                RsData.of("200", "Vocabulary updated successfully", response)
        );
    }

    @Operation(summary = "Delete vocabulary", description = "Deletes a vocabulary word")
    @DeleteMapping("/{vocabularyId}")
    public ResponseEntity<RsData<Void>> deleteVocabulary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId) {

        vocabularyService.deleteVocabulary(vocabularyId, userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "Vocabulary deleted successfully", null)
        );
    }

    @Operation(summary = "Update study status", description = "Updates only the study status of a vocabulary word")
    @PatchMapping("/{vocabularyId}/status")
    public ResponseEntity<RsData<VocabularyResponse>> updateStudyStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long vocabularyId,
            @RequestParam StudyStatus studyStatus) {

        VocabularyResponse response = vocabularyService
                .updateStudyStatus(vocabularyId, userDetails.getUsername(), studyStatus);

        return ResponseEntity.ok(
                RsData.of("200", "Study status updated successfully", response)
        );
    }
}