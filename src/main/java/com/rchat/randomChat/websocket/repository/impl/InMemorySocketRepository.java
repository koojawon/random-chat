package com.rchat.randomChat.websocket.repository.impl;

import com.rchat.randomChat.websocket.repository.WebSocketSessionRepository;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class InMemorySocketRepository implements WebSocketSessionRepository {

    ConcurrentHashMap<String, WebSocketSession> sessionRepository = new ConcurrentHashMap<>();

    @Override
    public void put(WebSocketSession session) {
        sessionRepository.put(session.getId(), session);
    }

    @Override
    public WebSocketSession get(String id) {
        return sessionRepository.get(id);
    }

    @Override
    public void remove(WebSocketSession session) {
        sessionRepository.remove(session.getId());
    }
}
