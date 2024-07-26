package com.rchat.randomChat.websocket.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

@Repository
public interface WebSocketSessionRepository {

    public void put(WebSocketSession session);

    public WebSocketSession get(String id);

    public void remove(WebSocketSession session);
}
