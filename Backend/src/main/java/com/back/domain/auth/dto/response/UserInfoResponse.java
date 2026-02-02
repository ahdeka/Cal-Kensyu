package com.back.domain.auth.dto.response;

import com.back.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserInfoResponse(
        Long id,
        String username,
        String email,
        String nickname,
        String role
) {
    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
    }
}