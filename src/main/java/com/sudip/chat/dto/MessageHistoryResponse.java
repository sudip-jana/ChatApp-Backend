package com.sudip.chat.dto;

import java.util.List;

public record MessageHistoryResponse(
        List<MessageResponse> messages,
        Long nextCursor,
        boolean hasMore
) {}