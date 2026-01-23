package com.back.domain.vocabulary.entity;

import lombok.Getter;

@Getter
public enum StudyStatus {
    NOT_STUDIED("Not Studied"),
    STUDYING("Studying"),
    COMPLETED("Completed");

    private final String displayName;

    StudyStatus(String displayName) {
        this.displayName = displayName;
    }
}