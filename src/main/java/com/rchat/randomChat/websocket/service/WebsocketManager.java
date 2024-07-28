package com.rchat.randomChat.websocket.service;

import com.rchat.randomChat.websocket.repository.WebSocketSessionRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class WebsocketManager {
    private final WebSocketSessionRepository sessionRepository;

    public void sendMessage(String sessionId, TextMessage message) throws IOException {
        WebSocketSession session = sessionRepository.get(sessionId);
        session.sendMessage(message);
    }

    public void join(WebSocketSession session) {
        sessionRepository.put(session);
    }

    public void leave(WebSocketSession session) {
        sessionRepository.remove(session);
    }

    public boolean checkAliveById(String sessionId) {
        return sessionRepository.get(sessionId).isOpen();
    }
}
