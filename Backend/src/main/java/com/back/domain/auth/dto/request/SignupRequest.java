package com.back.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "ユーザー名は必須です")
        String username,

        @NotBlank(message = "パスワードは必須です")
        String password,

        @NotBlank(message = "パスワード確認は必須です")
        String passwordConfirm,

        @NotBlank(message = "メールは必須です")
        @Email(message = "有効なメールアドレスを入力してください")
        String email,

        @NotBlank(message = "ニックネームは必須です")
        String nickname
) {
    // パスワード一致確認メソッド
    public boolean isPasswordMatching() {
        return password != null && password.equals(passwordConfirm);
    }
}