package com.back.domain.quiz.service;

import com.back.domain.quiz.entity.JlptLevel;
import com.back.domain.quiz.entity.QuizWord;
import com.back.domain.quiz.entity.WordSource;
import com.back.domain.quiz.repository.QuizWordRepository;
import com.back.global.exception.ServiceException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JlptCsvImportService {

    private final QuizWordRepository quizWordRepository;

    @Transactional
    public int importJlptCsv(MultipartFile file, JlptLevel jlptLevel) throws IOException, CsvException {
        List<String[]> csvData = parseCsv(file);

        if (csvData.isEmpty()) {
            throw new ServiceException("400", "CSVファイルが空です。");
        }

        // 헤더 제거 (첫 줄이 헤더인 경우)
        String[] header = csvData.get(0);
        boolean hasHeader = isHeader(header);
        if (hasHeader) {
            csvData.remove(0);
        }

        List<QuizWord> quizWords = new ArrayList<>();
        int skippedCount = 0;

        for (int i = 0; i < csvData.size(); i++) {
            String[] row = csvData.get(i);

            try {
                // CSV 형식: expression, reading, meaning (예상)
                if (row.length < 2) {
                    log.warn("行 {} をスキップ: カラム数不足 ({}個)", i + 1, row.length);
                    skippedCount++;
                    continue;
                }

                String word = row[0].trim();
                String hiragana = row.length > 1 ? row[1].trim() : "";
                String meaning = row.length > 2 ? row[2].trim() : "";

                if (word.isEmpty() || hiragana.isEmpty()) {
                    log.warn("行 {} をスキップ: 必須フィールドが空です", i + 1);
                    skippedCount++;
                    continue;
                }

                // 중복 체크
                boolean exists = quizWordRepository.existsBySourceAndSourceDetailAndWordAndHiragana(
                        WordSource.JLPT,
                        jlptLevel.name(),
                        word,
                        hiragana
                );

                if (exists) {
                    log.debug("行 {} をスキップ: 既に存在する単語 - {}", i + 1, word);
                    skippedCount++;
                    continue;
                }

                QuizWord quizWord = QuizWord.builder()
                        .source(WordSource.JLPT)
                        .sourceDetail(jlptLevel.name())
                        .word(word)
                        .hiragana(hiragana)
                        .meaning(meaning.isEmpty() ? "-" : meaning)
                        .build();

                quizWords.add(quizWord);

            } catch (Exception e) {
                log.error("行 {} の処理中にエラー: {}", i + 1, e.getMessage());
                skippedCount++;
            }
        }

        // Batch Insert
        if (!quizWords.isEmpty()) {
            quizWordRepository.saveAll(quizWords);
            log.info("JLPT {} 単語 {}個の登録完了 (スキップ: {}個)",
                    jlptLevel.name(), quizWords.size(), skippedCount);
        }

        return quizWords.size();
    }

    private List<String[]> parseCsv(MultipartFile file) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.readAll();
        }
    }

    private boolean isHeader(String[] row) {
        // 헤더 판단 로직
        if (row.length < 2) return false;

        String first = row[0].toLowerCase();
        return first.contains("expression") ||
                first.contains("word") ||
                first.contains("kanji") ||
                first.contains("単語");
    }

    @Transactional(readOnly = true)
    public long countJlptWords(JlptLevel level) {
        return quizWordRepository.countBySourceAndSourceDetail(
                WordSource.JLPT,
                level.name()
        );
    }

    @Transactional(readOnly = true)
    public long countAllJlptWords() {
        return quizWordRepository.countBySource(WordSource.JLPT);
    }
}