package com.back.global.initData;

import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.quiz.entity.JlptLevel;
import com.back.domain.quiz.entity.QuizWord;
import com.back.domain.quiz.entity.WordSource;
import com.back.domain.quiz.repository.QuizWordRepository;
import com.back.domain.quiz.service.JlptCsvImportService;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.vocabulary.entity.StudyStatus;
import com.back.domain.vocabulary.entity.Vocabulary;
import com.back.domain.vocabulary.repository.VocabularyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final VocabularyRepository vocabularyRepository;
    private final QuizWordRepository quizWordRepository;
    private final JlptCsvImportService jlptCsvImportService;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            createUsers();
            createDiaries();
            createVocabularies();
            createQuizWords();
        };
    }

    @Transactional
    public void createQuizWords() {
        if (quizWordRepository.count() > 0) {
            log.info("Quiz words already exist: skipping");
            return;
        }

        // Attempt to load JLPT words from CSV
        try {
            loadJlptWordsFromCsv();
        } catch (Exception e) {
            log.warn("Failed to load JLPT words from CSV.", e);
        }
    }

    @Transactional
    public void createDiaries() {
        // Retrieve users
        User user1 = userRepository.findByUsername("user1").orElse(null);
        User user2 = userRepository.findByUsername("user2").orElse(null);
        User user3 = userRepository.findByUsername("user3").orElse(null);

        if (user1 == null || user2 == null || user3 == null) {
            log.warn("Skipping diary generation: users do not exist");
            return;
        }

        // User1's diaries (2 public, 2 private)
        createDiary(user1, LocalDate.now().minusDays(1),
                "初めての日本語日記",
                "今日から日本語の勉強を始めました。最初は難しいですが、毎日少しずつ頑張ります。ひらがなとカタカナを覚えるのが目標です。",
                true);

        createDiary(user1, LocalDate.now().minusDays(3),
                "カフェで勉強",
                "近くのカフェで日本語を勉強しました。静かな環境で集中できて良かったです。新しい単語を20個覚えました。",
                true);

        createDiary(user1, LocalDate.now().minusDays(5),
                "難しい文法",
                "今日は助詞の使い方を勉強しました。「は」と「が」の違いが本当に難しいです。もっと練習が必要だと思います。",
                false);

        createDiary(user1, LocalDate.now().minusDays(7),
                "週末の計画",
                "週末に日本のドラマを見る予定です。字幕なしで見るのはまだ難しいですが、挑戦してみます。",
                false);

        // User2's diaries (2 public, 1 private)
        createDiary(user2, LocalDate.now().minusDays(2),
                "日本料理を作った",
                "今日、初めて日本料理を作りました。簡単なお味噌汁と卵焼きです。美味しくできて嬉しかったです。次は肉じゃがに挑戦したいです。",
                true);

        createDiary(user2, LocalDate.now().minusDays(4),
                "オンライン授業",
                "オンラインで日本語の授業を受けました。先生がとても優しくて、分かりやすく説明してくれました。会話の練習もできて良かったです。",
                true);

        createDiary(user2, LocalDate.now().minusDays(6),
                "漢字が難しい",
                "漢字を覚えるのが本当に大変です。同じ漢字でも読み方がたくさんあって混乱します。毎日10個ずつ覚える練習をしています。",
                false);

        // User3's diaries (3 public)
        createDiary(user3, LocalDate.now(),
                "今日の勉強",
                "今日は動詞の活用を勉強しました。て形とた形の作り方を練習しました。少しずつ慣れてきた気がします。明日も頑張ります！",
                true);

        createDiary(user3, LocalDate.now().minusDays(2),
                "日本の音楽",
                "最近、日本の音楽をよく聞いています。歌詞を読みながら聞くと、新しい表現を学べて楽しいです。おすすめの歌があれば教えてください。",
                true);

        createDiary(user3, LocalDate.now().minusDays(8),
                "友達と日本語で話した",
                "今日、日本人の友達と日本語だけで会話しました。まだ完璧ではありませんが、楽しくコミュニケーションできました。もっと上手になりたいです。",
                true);

        log.info("Sample diary generation completed: 10 diaries (user1: 4, user2: 3, user3: 3)");
    }

    @Transactional
    public void createUsers() {

        // 1. Create 1 admin account
        createUser("admin", "admin123!", "admin@nihongo.com", "管理者", Role.ADMIN);

        // 2. Create 5 regular user accounts
        for (int i = 1; i <= 5; i++) {
            String username = String.format("user%d", i);
            String email = String.format("user%d@test.com", i);
            String nickname = String.format("ユーザー%d", i);

            createUser(username, "user123!", email, nickname, Role.USER);
        }

        log.info("Total accounts created: 6 (1 admin, 5 regular users)");
    }

    @Transactional
    public void createVocabularies() {
        // Retrieve user
        User user1 = userRepository.findByUsername("user1").orElse(null);

        if (user1 == null) {
            log.warn("Skipping vocabulary generation: user does not exist");
            return;
        }

        createVocabulary(user1, "隣人", "りんじん", "이웃, 인인",
                "隣人の迷惑にならないように気をつけましょう。",
                "이웃에게 폐가 되지 않도록 조심합시다.",
                StudyStatus.COMPLETED);

        createVocabulary(user1, "日記", "にっき", "일기",
                "毎晩日記を書きます。",
                "매일 밤 일기를 씁니다.",
                StudyStatus.NOT_STUDIED);

        createVocabulary(user1, "難しい", "むずかしい", "어렵다",
                "この問題は難しいです。",
                "이 문제는 어렵습니다.",
                StudyStatus.STUDYING);

        createVocabulary(user1, "助詞", "じょし", "조사",
                "日本語の助詞は複雑です。",
                "일본어 조사는 복잡합니다.",
                StudyStatus.STUDYING);

        createVocabulary(user1, "字幕", "じまく", "자막",
                "字幕なしでドラマを見ます。",
                "자막 없이 드라마를 봅니다.",
                StudyStatus.NOT_STUDIED);

        createVocabulary(user1, "挑戦", "ちょうせん", "도전",
                "新しいことに挑戦します。",
                "새로운 것에 도전합니다.",
                StudyStatus.NOT_STUDIED);

        log.info("Vocabulary test data generation completed");
    }

    private void createUser(String username, String password, String email, String nickname, Role role) {
        // Skip if account already exists
        if (userRepository.existsByUsername(username)) {
            log.debug("Account already exists ({}): skipping", username);
            return;
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(password);

        // Create User entity
        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .nickname(nickname)
                .role(role)
                .build();

        // Save
        userRepository.save(user);
        log.info("User created: {} ({})", username, nickname);
    }

    private void createDiary(User user, LocalDate diaryDate, String title, String content, boolean isPublic) {
        // Skip if diary already exists
        if (diaryRepository.findByUserAndDiaryDate(user, diaryDate).isPresent()) {
            log.debug("Diary already exists (user: {}, date: {}): skipping", user.getUsername(), diaryDate);
            return;
        }

        Diary diary = Diary.builder()
                .user(user)
                .diaryDate(diaryDate)
                .title(title)
                .content(content)
                .isPublic(isPublic)
                .build();

        diaryRepository.save(diary);
        log.debug("Diary created: {} - {}", user.getUsername(), title);
    }

    private void createVocabulary(User user, String word, String hiragana, String meaning,
                                  String exampleSentence, String exampleTranslation,
                                  StudyStatus studyStatus) {
        Vocabulary vocabulary = Vocabulary.builder()
                .user(user)
                .word(word)
                .hiragana(hiragana)
                .meaning(meaning)
                .exampleSentence(exampleSentence)
                .exampleTranslation(exampleTranslation)
                .studyStatus(studyStatus)
                .build();

        vocabularyRepository.save(vocabulary);
    }

    private void loadJlptWordsFromCsv() {
        log.info("Loading JLPT word data from CSV...");

        int totalLoaded = 0;

        for (JlptLevel level : JlptLevel.values()) {
            try {
                String filename = level.name().toLowerCase() + ".csv";
                ClassPathResource resource = new ClassPathResource("data/jlpt/" + filename);

                if (!resource.exists()) {
                    log.warn("{} file not found: {}", level.name(), filename);
                    continue;
                }

                MultipartFile multipartFile = convertToMultipartFile(resource, filename);
                int count = jlptCsvImportService.importJlptCsv(multipartFile, level);
                totalLoaded += count;

                log.info("{} words registered: {}", level.name(), count);

            } catch (Exception e) {
                log.error("Error loading {} data: {}", level.name(), e.getMessage());
            }
        }

        if (totalLoaded == 0) {
            throw new RuntimeException("Failed to load words from CSV files");
        }

        long totalCount = quizWordRepository.countBySource(WordSource.JLPT);
        log.info("JLPT word data loading completed. Total words: {}", totalCount);
    }

    private MultipartFile convertToMultipartFile(ClassPathResource resource, String filename) throws IOException {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return filename;
            }

            @Override
            public String getContentType() {
                return "text/csv";
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                try {
                    return resource.contentLength();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public byte[] getBytes() throws IOException {
                return resource.getInputStream().readAllBytes();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return resource.getInputStream();
            }

            @Override
            public void transferTo(File dest) throws IOException {
                try (InputStream in = resource.getInputStream()) {
                    java.nio.file.Files.copy(in, dest.toPath());
                }
            }
        };
    }

    private void createQuizWord(WordSource source, String sourceDetail,
                                String word, String hiragana, String meaning) {
        QuizWord quizWord = QuizWord.builder()
                .source(source)
                .sourceDetail(sourceDetail)
                .word(word)
                .hiragana(hiragana)
                .meaning(meaning)
                .exampleSentence(null)
                .exampleTranslation(null)
                .build();

        quizWordRepository.save(quizWord);
    }
}