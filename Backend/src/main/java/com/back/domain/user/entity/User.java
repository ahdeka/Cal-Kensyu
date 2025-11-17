package com.back.domain.user.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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
    private String name; // 名前

    @Column(length = 50)
    private String nickname;
}
