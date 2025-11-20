package com.back.domain.auth.dto.response;

import lombok.Builder;

@Builder
public record UserInfoResponse(
        Long id,
        String username,
        String email,
        String nickname,
        String role
) {}