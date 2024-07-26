package com.rchat.randomChat.websocket.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rchat.randomChat.websocket.repository.WebSocketSessionRepository;
import com.rchat.randomChat.websocket.statics.SdpGenerationOrderMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
public class SendService {

    private final WebSocketSessionRepository webSocketSessionRepository;
    private final Gson gson = new Gson();

    public void sendSdp(String opponentId, JsonObject jsonObject) throws IOException {
        WebSocketSession session = webSocketSessionRepository.get(opponentId);
        String message = gson.toJson(jsonObject);
        session.sendMessage(new TextMessage(message));
    }

    public void orderGenerateOffer(String id) throws IOException {
        WebSocketSession session = webSocketSessionRepository.get(id);
        String message = gson.toJson(new SdpGenerationOrderMessage());
        session.sendMessage(new TextMessage(message));
    }

    public void sendIceCandidate(String id, JsonObject jsonObject) throws IOException {
        WebSocketSession session = webSocketSessionRepository.get(id);
        String message = gson.toJson(jsonObject);
        session.sendMessage(new TextMessage(message));
    }

    public void sendStop(String opponentId, JsonObject jsonObject) throws IOException {
        WebSocketSession session = webSocketSessionRepository.get(opponentId);
        String message = gson.toJson(jsonObject);
        session.sendMessage(new TextMessage(message));
    }

    public boolean checkAlive(String id) {
        WebSocketSession session = webSocketSessionRepository.get(id);

        return session.isOpen();
    }
}
