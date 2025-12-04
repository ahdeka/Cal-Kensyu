package com.back.domain.quiz.entity;

public enum WordSource {
    JLPT("JLPT公式単語"),
    USER_VOCABULARY("ユーザー単語帳"),
    USER_DIARY("ユーザー日記");

    private final String displayName;

    WordSource(String displayName) {
        this.displayName = displayName;
    }
}
