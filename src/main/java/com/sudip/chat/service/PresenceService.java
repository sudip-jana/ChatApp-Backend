package com.sudip.chat.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sudip.chat.model.UserStatus;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // One username may have multiple active sessions.
    private final ConcurrentMap<String, String> sessionUsers =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Set<String>> userSessions =
            new ConcurrentHashMap<>();

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        String username = accessor.getFirstNativeHeader("username");

        if (sessionId == null || username == null
                || username.isBlank()) {
            return;
        }

        // Associate each session with its user.
        sessionUsers.put(sessionId, username);

        Set<String> sessions = userSessions.computeIfAbsent(
                username, key -> ConcurrentHashMap.newKeySet());

        boolean firstSession = sessions.isEmpty();
        sessions.add(sessionId);

        if (firstSession) {
            publish(username, true);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String username = sessionUsers.remove(sessionId);

        if (username == null) {
            return;
        }

        Set<String> sessions = userSessions.get(username);

        if (sessions != null) {
            sessions.remove(sessionId);

            if (sessions.isEmpty()
                    && userSessions.remove(username, sessions)) {
                publish(username, false);
            }
        }
    }

    private void publish(String username, boolean online) {
        messagingTemplate.convertAndSend(
                "/topic/presence",
                new UserStatus(username, online)
        );
    }
}
