package com.rchat.randomChat.websocket.service;

import com.google.gson.Gson;
import com.rchat.randomChat.websocket.repository.WebSocketSessionRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsocketManager {
    private final WebSocketSessionRepository sessionRepository;
    private final Gson gson = new Gson();

    public void sendMessage(String sessionId, Object message) throws IOException {
        WebSocketSession session = sessionRepository.get(sessionId);
        session.sendMessage(new TextMessage(gson.toJson(message)));
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
