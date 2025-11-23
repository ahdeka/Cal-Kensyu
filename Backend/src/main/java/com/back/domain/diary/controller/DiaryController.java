package com.back.domain.diary.controller;

import com.back.domain.diary.dto.request.DiaryCreateRequest;
import com.back.domain.diary.dto.request.DiaryUpdateRequest;
import com.back.domain.diary.dto.response.DiaryListResponse;
import com.back.domain.diary.dto.response.DiaryResponse;
import com.back.domain.diary.service.DiaryService;
import com.back.global.rsData.RsData;
import com.back.global.security.auth.CustomUserDetails;
import com.back.global.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<RsData<DiaryResponse>> createDiary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DiaryCreateRequest request) {

        String username = userDetails.getUsername();
        DiaryResponse response = diaryService.createDiary(username, request);

        return ResponseEntity.ok(
                RsData.of("201", "日記作成が完了しました", response)
        );
    }

    @GetMapping("/public")
    public ResponseEntity<RsData<List<DiaryListResponse>>> getPublicDiaries() {
        List<DiaryListResponse> diaries = diaryService.getPublicDiaries();

        return ResponseEntity.ok(
                RsData.of("200", "公開日記リスト取得成功", diaries)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<RsData<List<DiaryListResponse>>> getMyDiaries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        List<DiaryListResponse> diaries = diaryService.getMyDiaries(username);

        return ResponseEntity.ok(
                RsData.of("200", "日記リスト取得成功", diaries)
        );
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<RsData<DiaryResponse>> getDiary(
            @PathVariable Long diaryId,
            @CookieValue(name = "accessToken", required = false) String accessToken) {

        String username = null;
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            username = jwtTokenProvider.getUsername(accessToken);
        }

        DiaryResponse response = diaryService.getDiary(diaryId, username);

        return ResponseEntity.ok(
                RsData.of("200", "日記取得成功", response)
        );
    }

    @PutMapping("/{diaryId}")
    public ResponseEntity<RsData<DiaryResponse>> updateDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DiaryUpdateRequest request) {

        String username = userDetails.getUsername();
        DiaryResponse response = diaryService.updateDiary(diaryId, username, request);

        return ResponseEntity.ok(
                RsData.of("200", "日記修正が完了しました", response)
        );
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<RsData<Void>> deleteDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String username = userDetails.getUsername();
        diaryService.deleteDiary(diaryId, username);

        return ResponseEntity.ok(
                RsData.of("200", "日記削除が完了しました")
        );
    }
}
