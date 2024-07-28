package com.rchat.randomChat.websocket.service;

import com.google.gson.Gson;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
public class SendService {

    private final Gson gson = new Gson();

    public void sendMessage(WebSocketSession session, Object jsonObject) throws IOException {
        String message = gson.toJson(jsonObject);
        session.sendMessage(new TextMessage(message));
    }
}
