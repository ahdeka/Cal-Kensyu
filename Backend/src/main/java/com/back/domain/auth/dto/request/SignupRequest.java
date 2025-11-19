package com.back.domain.auth.dto.request;

public record SignupRequest(
        String username,
        String password,
        String email,
        String name,
        String nickname
) {}