package com.sudip.chat.dto;

import com.sudip.chat.model.MessageStatus;

import java.time.Instant;

public record MessageResponse(
        Long id,
        String sender,
        String receiver,
        String content,
        Instant createdAt,
        MessageStatus status
) {}