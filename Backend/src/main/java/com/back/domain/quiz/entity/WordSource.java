package com.back.domain.quiz.entity;

public enum WordSource {
    JLPT("JLPT Official Words"),
    USER_VOCABULARY("User Vocabulary"),
    USER_DIARY("User Diary");

    private final String displayName;

    WordSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}