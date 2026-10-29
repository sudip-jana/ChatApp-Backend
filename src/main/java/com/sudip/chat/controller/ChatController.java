package com.sudip.chat.controller;

import com.sudip.chat.dto.SendMessageRequest;
import com.sudip.chat.entity.Message;
import com.sudip.chat.service.ChatService;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatController(
            SimpMessagingTemplate messagingTemplate,
            ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(
            SendMessageRequest request, Principal principal) {

        if (principal == null) {
            throw new IllegalStateException("User is not connected");
        }

        Message message = chatService.saveMessage(
                principal.getName(),
                request.receiver(),
                request.content()
        );

        // Route only to the intended recipient.
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(),
                "/queue/messages",
                message
        );
    }

    @MessageMapping("/chat.pending")
    public void sendPendingMessages(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("User is not connected");
        }

        for (Message message :
                chatService.getPendingMessages(principal.getName())) {

            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/messages",
                    message
            );
        }
    }

    @MessageMapping("/chat.ack")
    public void acknowledgeDelivery(
            Long messageId, Principal principal) {

        if (principal == null) {
            throw new IllegalStateException("User is not connected");
        }

        chatService.markDelivered(messageId, principal.getName());
    }
}

