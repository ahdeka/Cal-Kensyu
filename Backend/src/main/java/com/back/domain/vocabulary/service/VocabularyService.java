package com.back.domain.vocabulary.service;

import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;

    @Transactional
    public VocabularyResponse createVocabulary(String username, VocabularyCreateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        List<Vocabulary> vocabularies = vocabularyRepository.findByUserOrderByCreateDateDesc(user);

        return vocabularies.stream()
                .map(VocabularyListResponse::from)
                .collect(Collectors.toList());
    }

    public List<VocabularyListResponse> getVocabulariesByStatus(String username, StudyStatus studyStatus) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        List<Vocabulary> vocabularies = vocabularyRepository
                .findByUserAndStudyStatusOrderByCreateDateDesc(user, studyStatus);

        return vocabularies.stream()
                .map(VocabularyListResponse::from)
                .collect(Collectors.toList());
    }

    public List<VocabularyListResponse> searchVocabularies(String username, String keyword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        List<Vocabulary> vocabularies = vocabularyRepository
                .searchByKeyword(user, keyword);

        return vocabularies.stream()
                .map(VocabularyListResponse::from)
                .collect(Collectors.toList());
    }

    public VocabularyResponse getVocabulary(Long vocabularyId, String username) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ServiceException("404", "単語が見つかりません"));

        if (!vocabulary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この単語を閲覧する権限がありません");
        }

        return VocabularyResponse.from(vocabulary);
    }

    @Transactional
    public VocabularyResponse updateVocabulary(Long vocabularyId, String username, VocabularyUpdateRequest request) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ServiceException("404", "単語が見つかりません"));

        if (!vocabulary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この単語を修正する権限がありません");
        }

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
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ServiceException("404", "単語が見つかりません"));

        if (!vocabulary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この単語を削除する権限がありません");
        }

        vocabularyRepository.delete(vocabulary);
    }

    @Transactional
    public VocabularyResponse updateStudyStatus(Long vocabularyId, String username, StudyStatus studyStatus) {
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ServiceException("404", "単語が見つかりません"));

        if (!vocabulary.getUser().getUsername().equals(username)) {
            throw new ServiceException("403", "この単語の学習状態を変更する権限がありません");
        }

        vocabulary.updateStudyStatus(studyStatus);

        return VocabularyResponse.from(vocabulary);
    }
}
