package com.sudip.chat.controller;

import com.sudip.chat.dto.MessageHistoryResponse;
import com.sudip.chat.service.MessageHistoryService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Locale;

@RestController
@RequestMapping("/api/messages")
public class MessageHistoryController {

    private final MessageHistoryService messageHistoryService;

    public MessageHistoryController(
            MessageHistoryService messageHistoryService) {
        this.messageHistoryService = messageHistoryService;
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
}