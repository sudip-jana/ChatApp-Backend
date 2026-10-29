package com.sudip.chat.model;

public record ChatMessage(
        String sender,
        String receiver,
        String content,
        long timestamp
) {}
