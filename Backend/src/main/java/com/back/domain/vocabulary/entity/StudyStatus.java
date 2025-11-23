package com.back.domain.vocabulary.entity;

import lombok.Getter;

@Getter
public enum StudyStatus {
    NOT_STUDIED("学習前"),
    STUDYING("学習中"),
    COMPLETED("学習完了");

    private final String displayName;

    StudyStatus(String displayName) {
        this.displayName = displayName;
    }
}
