package com.rchat.randomChat.websocket.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rchat.randomChat.match.service.MatchManager;
import com.rchat.randomChat.websocket.service.WebsocketManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    private final Gson gson = new Gson();
    private final MatchManager matchManager;
    private final WebsocketManager websocketManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        websocketManager.join(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonObject = gson.fromJson(message.getPayload(), JsonObject.class);
        switch (jsonObject.get("id").getAsString()) {
            case "start":
                matchManager.joinQueue(session);
                break;
            case "sdpOffer":
            case "sdpAnswer":
            case "onIceCandidate": {
                String opponentId = matchManager.getOpponentId(session);
                websocketManager.sendMessage(opponentId, message);
            }
            break;
            case "stop":
                if (matchManager.isAfterMatched(session)) {
                    matchManager.getOpponentId(session);
                    matchManager.deCouple(session);
                }
                matchManager.withdraw(session);
                break;
            default:
                log.error("Message with Unknown ID received : {}", jsonObject);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        websocketManager.leave(session);
    }
}
