package com.sudip.chat.service;

import com.sudip.chat.dto.MessageHistoryResponse;
import com.sudip.chat.dto.MessageResponse;
import com.sudip.chat.entity.Message;
import com.sudip.chat.repository.AppUserRepository;
import com.sudip.chat.repository.MessageRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
public class MessageHistoryService {

    private final MessageRepository messageRepository;
    private final AppUserRepository userRepository;

    public MessageHistoryService(
            MessageRepository messageRepository,
            AppUserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public MessageHistoryResponse getHistory(
            String currentUser,
            String otherUser,
            Long beforeId,
            int requestedSize) {

        if (otherUser == null || otherUser.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipient username is required"
            );
        }

        if (!userRepository.existsByUsername(otherUser)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "USER_NOT_FOUND"
            );
        }

        if (beforeId != null && beforeId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid message cursor"
            );
        }

        int size = Math.max(1, Math.min(requestedSize, 100));

        // Fetch one extra message to determine whether older messages exist.
        List<Message> fetched =
                messageRepository.findConversationBeforeId(
                        currentUser,
                        otherUser,
                        beforeId,
                        PageRequest.of(0, size + 1)
                );

        boolean hasMore = fetched.size() > size;

        if (hasMore) {
            fetched = fetched.subList(0, size);
        }

        // Repository returns newest first. Reverse for normal chat display.
        Collections.reverse(fetched);

        List<MessageResponse> messages = fetched.stream()
                .map(this::toResponse)
                .toList();

        Long nextCursor = hasMore && !fetched.isEmpty()
                ? fetched.get(0).getId()
                : null;

        return new MessageHistoryResponse(
                messages,
                nextCursor,
                hasMore
        );
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender(),
                message.getReceiver(),
                message.getContent(),
                message.getCreatedAt(),
                message.getStatus()
        );
    }
}