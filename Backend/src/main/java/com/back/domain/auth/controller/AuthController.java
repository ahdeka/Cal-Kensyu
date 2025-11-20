package com.back.domain.auth.controller;

import com.back.domain.auth.dto.request.LoginRequest;
import com.back.domain.auth.dto.request.SignupRequest;
import com.back.domain.auth.dto.response.UserInfoResponse;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int ACCESS_TOKEN_COOKIE_MAX_AGE = 60 * 60;
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 60 * 60 * 24 * 7;

    @PostMapping("/login")
    public ResponseEntity<RsData<Void>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String accessToken = jwtTokenProvider.createAccessToken(request.username());
        String refreshToken = jwtTokenProvider.createRefreshToken(request.username());

        // DBにrefreshTokenを保存
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        // Cookieに保存
        Cookie accessTokenCookie = createCookie("accessToken", accessToken, ACCESS_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = createCookie("refreshToken", refreshToken, REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(
                RsData.of("200", "ログイン成功")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<RsData<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // DBからrefreshTokenを削除
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElse(null);

            if (user != null) {
                user.destroyRefreshToken();
                userRepository.save(user);
            }
        }

        // Cookieを削除
        Cookie accessTokenCookie = createCookie("accessToken", null, 0);
        Cookie refreshTokenCookie = createCookie("refreshToken", null, 0);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(
                RsData.of("200", "ログアウト成功")
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RsData<Void>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new ServiceException("401", "有効ではないトークンです");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        // DBに保存されているrefreshTokenと比較
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new ServiceException("401", "トークンが一致しません");
        }

        // 新しいアクセストークンを生成
        String newAccessToken = jwtTokenProvider.createAccessToken(username);

        Cookie accessTokenCookie = createCookie("accessToken", newAccessToken, ACCESS_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(
                RsData.of("200", "トークン更新成功")
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<RsData<Void>> signup(@Valid @RequestBody SignupRequest request) {
        if (!request.isPasswordMatching()) {
            throw new ServiceException("400", "パスワードが一致しません");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ServiceException("400", "既に存在するユーザー名です");
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new ServiceException("400", "既に存在するニックネームです");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ServiceException("400", "既に存在するメールアドレスです");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .nickname(request.nickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(
                RsData.of("201", "会員登録が完了しました")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<RsData<UserInfoResponse>> getCurrentUser(
            @CookieValue(name = "accessToken", required = false) String accessToken) {

        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            throw new ServiceException("401", "認証が必要です");
        }

        String username = jwtTokenProvider.getUsername(accessToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404", "ユーザーが見つかりません"));

        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(
                RsData.of("200", "ユーザー情報取得成功", userInfo)
        );
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setDomain("localhost");
        return cookie;
    }
}