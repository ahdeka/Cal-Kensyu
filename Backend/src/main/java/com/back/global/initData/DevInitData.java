package com.back.global.initData;

import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            createUsers();
        };
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
}
