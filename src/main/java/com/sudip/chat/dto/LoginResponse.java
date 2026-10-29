package com.sudip.chat.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
