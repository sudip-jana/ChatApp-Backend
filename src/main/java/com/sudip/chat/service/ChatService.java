package com.sudip.chat.service;

import com.sudip.chat.entity.Message;
import com.sudip.chat.model.MessageStatus;
import com.sudip.chat.repository.AppUserRepository;
import com.sudip.chat.repository.MessageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final MessageRepository messageRepository;
    private final AppUserRepository userRepository;

    public ChatService(MessageRepository messageRepository, AppUserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Message saveMessage(
            String sender, String receiver, String content) {

        if (receiver == null || receiver.isBlank()) {
            throw new IllegalArgumentException("Receiver is required");
        }

        if (content == null || content.isBlank()
                || content.length() > 5000) {
            throw new IllegalArgumentException(
                    "Message must contain 1-5000 characters");
        }

        if (sender.equals(receiver)) {
            throw new IllegalArgumentException(
                    "Cannot send a message to yourself");
        }

        if (!userRepository.existsByUsername(receiver)) {
            throw new IllegalArgumentException(
                    "Recipient username does not exist");
        }

        return messageRepository.save(
                new Message(sender, receiver, content.trim()));
    }

    public List<Message> getPendingMessages(String username) {
        return messageRepository
                .findByReceiverAndStatusOrderByCreatedAtAsc(
                        username, MessageStatus.SENT);
    }

    @Transactional
    public void markDelivered(Long messageId, String receiver) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Message not found"));

        if (!message.getReceiver().equals(receiver)) {
            throw new IllegalArgumentException(
                    "Only the receiver can acknowledge this message");
        }

        if (message.getStatus() == MessageStatus.SENT) {
            message.setStatus(MessageStatus.DELIVERED);
        }
    }

    public List<Message> getConversation(
            String user1, String user2) {

        return messageRepository
                .findBySenderAndReceiverOrSenderAndReceiverOrderByCreatedAtAsc(
                        user1, user2, user2, user1);
    }
}
