package com.back.domain.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username; // Login ID

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime deleteDate;

    @Builder
    public User(String username, String password, String email, String nickname, Role role, UserStatus status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role != null ? role : Role.USER;
        this.status = status != null ? status : UserStatus.ACTIVE; // Default status
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    // Soft delete user account
    public void delete() {
        this.status = UserStatus.DELETED;
        this.deleteDate = LocalDateTime.now();
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void restore() {
        this.status = UserStatus.ACTIVE;
        this.deleteDate = null;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
}
