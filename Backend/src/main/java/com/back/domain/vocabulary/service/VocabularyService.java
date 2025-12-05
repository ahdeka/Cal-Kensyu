package com.back.domain.vocabulary.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.service.UserService;
import com.back.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.back.domain.vocabulary.dto.request.VocabularyUpdateRequest;
import com.back.domain.vocabulary.dto.response.VocabularyListResponse;
import com.back.domain.vocabulary.dto.response.VocabularyResponse;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import com.back.domain.vocabulary.repository.VocabularyRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 単語帳サービス
 * 単語の登録、照会、修正、削除機能を提供
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final UserService userService;

    @Transactional
    public VocabularyResponse createVocabulary(String username, VocabularyCreateRequest request) {
        User user = userService.getUserByUsername(username);

        Vocabulary vocabulary = Vocabulary.builder()
                .user(user)
                .word(request.word())
                .hiragana(request.hiragana())
                .meaning(request.meaning())
                .exampleSentence(request.exampleSentence())
                .exampleTranslation(request.exampleTranslation())
                .studyStatus(StudyStatus.NOT_STUDIED)
                .build();

        Vocabulary savedVocabulary = vocabularyRepository.save(vocabulary);

        return VocabularyResponse.from(savedVocabulary);
    }

    public List<VocabularyListResponse> getMyVocabularies(String username) {
        User user = userService.getUserByUsername(username);

        List<Vocabulary> vocabularies = vocabularyRepository.findByUserOrderByCreateDateDesc(user);

        return vocabularies.stream()
                .map(VocabularyListResponse::from)
                .collect(Collectors.toList());
    }

    public List<VocabularyListResponse> getVocabulariesByStatus(String username, StudyStatus studyStatus) {
        User user = userService.getUserByUsername(username);

        List<Vocabulary> vocabularies = vocabularyRepository
                .findByUserAndStudyStatusOrderByCreateDateDesc(user, studyStatus);

        return vocabularies.stream()
                .map(VocabularyListResponse::from)
                .collect(Collectors.toList());
    }

    public List<VocabularyListResponse> searchVocabularies(String username, String keyword) {
        User user = userService.getUserByUsername(username);

        List<Vocabulary> vocabularies = vocabularyRepository
                .searchByKeyword(user, keyword);

        return vocabularies.stream()
                .map(VocabularyListResponse::from)
                .collect(Collectors.toList());
    }

    public VocabularyResponse getVocabulary(Long vocabularyId, String username) {
        Vocabulary vocabulary = getVocabularyById(vocabularyId);

        userService.validateOwnership(vocabulary.getUser().getUsername(), username);

        return VocabularyResponse.from(vocabulary);
    }

    @Transactional
    public VocabularyResponse updateVocabulary(Long vocabularyId, String username, VocabularyUpdateRequest request) {
        Vocabulary vocabulary = getVocabularyById(vocabularyId);

        userService.validateOwnership(vocabulary.getUser().getUsername(), username);

        vocabulary.update(
                request.word(),
                request.hiragana(),
                request.meaning(),
                request.exampleSentence(),
                request.exampleTranslation(),
                request.studyStatus()
        );

        return VocabularyResponse.from(vocabulary);
    }

    @Transactional
    public void deleteVocabulary(Long vocabularyId, String username) {
        Vocabulary vocabulary = getVocabularyById(vocabularyId);

        userService.validateOwnership(vocabulary.getUser().getUsername(), username);

        vocabularyRepository.delete(vocabulary);
    }

    @Transactional
    public VocabularyResponse updateStudyStatus(Long vocabularyId, String username, StudyStatus studyStatus) {
        Vocabulary vocabulary = getVocabularyById(vocabularyId);

        userService.validateOwnership(vocabulary.getUser().getUsername(), username);

        vocabulary.updateStudyStatus(studyStatus);

        return VocabularyResponse.from(vocabulary);
    }

    // ===== Helper Method ===== /
    private Vocabulary getVocabularyById(Long vocabularyId) {
        return vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ServiceException("404", "単語が見つかりません"));
    }
}
