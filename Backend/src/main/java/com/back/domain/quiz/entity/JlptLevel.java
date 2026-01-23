package com.back.domain.quiz.entity;

import com.back.global.exception.ServiceException;

public enum JlptLevel {
    N5("N5", 1),
    N4("N4", 2),
    N3("N3", 3),
    N2("N2", 4),
    N1("N1", 5);

    private final String level;
    private final int difficulty;

    JlptLevel(String level, int difficulty) {
        this.level = level;
        this.difficulty = difficulty;
    }

    public String getLevel() {
        return level;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public static JlptLevel fromString(String level) {
        if (level == null || level.trim().isEmpty()) {
            throw new ServiceException("400", "JLPT level is empty.");
        }

        String normalized = level.trim().toUpperCase();
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("400", "Invalid JLPT level: " + level);
        }
    }
}