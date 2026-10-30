package com.sudip.chat.dto;

public record ConversationDto(
        String peer,
        MessageResponse lastMessage
) {}
