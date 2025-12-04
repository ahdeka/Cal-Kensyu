package com.back.global.initData;

import com.back.domain.diary.entity.Diary;
import com.back.domain.diary.repository.DiaryRepository;
import com.back.domain.quiz.entity.QuizWord;
import com.back.domain.quiz.entity.WordSource;
import com.back.domain.quiz.repository.QuizWordRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final PasswordEncoder passwordEncoder;
    private final VocabularyRepository  vocabularyRepository;
    private final QuizWordRepository  quizWordRepository;

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
            log.info("Quiz単語既に存在: スキップ");
            return;
        }

        createQuizWord(WordSource.JLPT, "N5", "食べる", "たべる", "먹다");
        createQuizWord(WordSource.JLPT, "N5", "学校", "がっこう", "학교");
        createQuizWord(WordSource.JLPT, "N5", "本", "ほん", "책");
        createQuizWord(WordSource.JLPT, "N5", "明日", "あした", "내일");
        createQuizWord(WordSource.JLPT, "N5", "見る", "みる", "보다");
        createQuizWord(WordSource.JLPT, "N5", "行く", "いく", "가다");
        createQuizWord(WordSource.JLPT, "N5", "水", "みず", "물");
        createQuizWord(WordSource.JLPT, "N5", "大きい", "おおきい", "크다");
        createQuizWord(WordSource.JLPT, "N5", "友達", "ともだち", "친구");
        createQuizWord(WordSource.JLPT, "N5", "買う", "かう", "사다");

        createQuizWord(WordSource.JLPT, "N3", "設立", "せつりつ", "설립");
        createQuizWord(WordSource.JLPT, "N3", "効率", "こうりつ", "효율");
        createQuizWord(WordSource.JLPT, "N3", "貿易", "ぼうえき", "무역");
        createQuizWord(WordSource.JLPT, "N3", "政策", "せいさく", "정책");
        createQuizWord(WordSource.JLPT, "N3", "普及", "ふきゅう", "보급");
        createQuizWord(WordSource.JLPT, "N3", "保障", "ほしょう", "보장");
        createQuizWord(WordSource.JLPT, "N3", "傾向", "けいこう", "경향");
        createQuizWord(WordSource.JLPT, "N3", "規模", "きぼ", "규모");
        createQuizWord(WordSource.JLPT, "N3", "著しい", "いちじるしい", "현저하다");
        createQuizWord(WordSource.JLPT, "N3", "拡大", "かくだい", "확대");

        log.info("Quiz単語生成完了: N5 10個, N3 10個");
    }

    @Transactional
    public void createDiaries() {
        // 사용자 조회
        User user1 = userRepository.findByUsername("user1").orElse(null);
        User user2 = userRepository.findByUsername("user2").orElse(null);
        User user3 = userRepository.findByUsername("user3").orElse(null);

        if (user1 == null || user2 == null || user3 == null) {
            log.warn("日記生成スキップ: ユーザーが存在しません");
            return;
        }

        // User1의 일기 4개 (공개 2개, 비공개 2개)
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

        // User2의 일기 3개 (공개 2개, 비공개 1개)
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

        // User3의 일기 3개 (공개 3개)
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

        log.info("サンプル日記生成完了: 10個 (user1: 4個, user2: 3個, user3: 3個)");
    }

    @Transactional
    public void createUsers() {

        // 1. 管理者アカウント 1個生成
        createUser("admin", "admin123!", "admin@nihongo.com", "管理者", Role.ADMIN);

        // 2. 一般ユーザーアカウント 5個生成
        for (int i = 1; i <= 5; i++) {
            String username = String.format("user%d", i);
            String email = String.format("user%d@test.com", i);
            String nickname = String.format("ユーザー%d", i);

            createUser(username, "user123!", email, nickname, Role.USER);
        }

        log.info("総生成アカウント: 6個 (管理者 1個、一般ユーザー 5個)");
    }

    @Transactional
    public void createVocabularies() {
        // 사용자 조회
        User user1 = userRepository.findByUsername("user1").orElse(null);

        if (user1 == null) {
            log.warn("単語帳生成スキップ: ユーザーが存在しません");
            return;
        }

        createVocabulary(user1, "隣人", "りんじん", "이웃, 인인",
                "隣人の迷惑にならないように気をつけましょう。。",
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

        log.info("単語帳テストデータ生成完了");
    }

    private void createUser(String username, String password, String email, String nickname, Role role) {
        // 既に存在するアカウントはスキップ
        if (userRepository.existsByUsername(username)) {
            log.debug("アカウント既に存在 ({}): スキップ", username);
            return;
        }

        // パスワード暗号化
        String encodedPassword = passwordEncoder.encode(password);

        // User エンティティ生成
        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .nickname(nickname)
                .role(role)
                .build();

        // 保存
        userRepository.save(user);
        log.info("ユーザー生成: {} ({})", username, nickname);
    }

    private void createDiary(User user, LocalDate diaryDate, String title, String content, boolean isPublic) {
        // 이미 존재하는 일기는 스킵
        if (diaryRepository.findByUserAndDiaryDate(user, diaryDate).isPresent()) {
            log.debug("日記既に存在 (ユーザー: {}, 日付: {}): スキップ", user.getUsername(), diaryDate);
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
        log.debug("日記生成: {} - {}", user.getUsername(), title);
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
