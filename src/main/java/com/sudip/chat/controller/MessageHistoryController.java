package com.sudip.chat.controller;

import com.sudip.chat.dto.ConversationDto;
import com.sudip.chat.dto.MessageHistoryResponse;
import com.sudip.chat.dto.MessageResponse;
import com.sudip.chat.repository.MessageRepository;
import com.sudip.chat.service.MessageHistoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/messages")
public class MessageHistoryController {

    private final MessageHistoryService messageHistoryService;
    private final MessageRepository messageRepository;

    public MessageHistoryController(
            MessageHistoryService messageHistoryService,
            MessageRepository messageRepository) {
        this.messageHistoryService = messageHistoryService;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/with/{username}")
    public MessageHistoryResponse getHistory(
            @PathVariable String username,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "30") int size,
            Principal principal) {

        String currentUser = principal.getName();
        String otherUser = username.toLowerCase(Locale.ROOT);

        return messageHistoryService.getHistory(
                currentUser,
                otherUser,
                beforeId,
                size
        );
    }

    @GetMapping("/conversations")
    public List<ConversationDto> conversations(Principal principal) {

        String me = principal.getName();

        return messageRepository.findLatestPerConversation(me)
                .stream()
                .map(message -> new ConversationDto(
                        message.getSender().equals(me)
                                ? message.getReceiver()
                                : message.getSender(),

                        new MessageResponse(
                                message.getId(),
                                message.getSender(),
                                message.getReceiver(),
                                message.getContent(),
                                message.getCreatedAt(),
                                message.getStatus()
                        )
                ))
                .toList();
    }
}