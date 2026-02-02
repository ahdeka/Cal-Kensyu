package com.back.domain.user.entity;

public enum UserStatus {
    ACTIVE,      // Active user
    DELETED,     // Soft deleted (can be restored within retention period)
    SUSPENDED    // Temporarily suspended by admin
}
