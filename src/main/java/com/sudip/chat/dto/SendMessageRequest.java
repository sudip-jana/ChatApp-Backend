package com.sudip.chat.dto;

public record SendMessageRequest(
        String receiver,
        String content
) {}

