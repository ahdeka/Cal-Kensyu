package com.back.domain.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String username; // ログインID

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 500)
    private String refreshToken;

    @Builder
    public User(String username, String password, String email, String nickname, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role != null ? role : Role.USER;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken() {
        this.refreshToken = null;
    }
}
