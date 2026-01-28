package com.back.domain.diary.controller;

import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.dto.response.DiaryListResponse;
import com.back.domain.diary.dto.response.DiaryResponse;
import com.back.domain.diary.service.DiaryService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "Diary API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "Create diary", description = "Creates a new diary entry")
    public ResponseEntity<RsData<DiaryResponse>> createDiary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DiaryCreateRequest request) {

        DiaryResponse response = diaryService.createDiary(userDetails.getUsername(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RsData.of("201", "Diary created successfully", response));
    }

    @GetMapping("/public")
    @Operation(summary = "Get public diaries", description = "Retrieves all public diary entries")
    public ResponseEntity<RsData<List<DiaryListResponse>>> getPublicDiaries() {
        List<DiaryListResponse> diaries = diaryService.getPublicDiaries();

        return ResponseEntity.ok(
                RsData.of("200", "Public diary list retrieved successfully", diaries)
        );
    }

    @GetMapping("/my")
    @Operation(summary = "Get my diaries", description = "Retrieves all diary entries of the current user")
    public ResponseEntity<RsData<List<DiaryListResponse>>> getMyDiaries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<DiaryListResponse> diaries = diaryService.getMyDiaries(
                userDetails.getUsername()
        );

        return ResponseEntity.ok(
                RsData.of("200", "Diary list retrieved successfully", diaries)
        );
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "Get diary", description = "Retrieves a specific diary entry")
    public ResponseEntity<RsData<DiaryResponse>> getDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails) {

        String username = (userDetails != null) ? userDetails.getUsername() : null;
        DiaryResponse response = diaryService.getDiary(diaryId, username);

        return ResponseEntity.ok(
                RsData.of("200", "Diary retrieved successfully", response)
        );
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "Update diary", description = "Updates an existing diary entry")
    public ResponseEntity<RsData<DiaryResponse>> updateDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DiaryUpdateRequest request) {

        DiaryResponse response = diaryService.updateDiary(diaryId, userDetails.getUsername(), request);

        return ResponseEntity.ok(
                RsData.of("200", "Diary updated successfully", response)
        );
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "Delete diary", description = "Deletes a diary entry")
    public ResponseEntity<RsData<Void>> deleteDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        diaryService.deleteDiary(diaryId, userDetails.getUsername());

        return ResponseEntity.ok(
                RsData.of("200", "Diary deleted successfully")
        );
    }
}