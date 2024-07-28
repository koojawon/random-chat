package com.rchat.randomChat.websocket.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rchat.randomChat.match.service.MatchService;
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
    private final MatchService matchService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        matchService.join(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonObject = gson.fromJson(message.getPayload(), JsonObject.class);
        switch (jsonObject.get("id").getAsString()) {
            case "start":
                matchService.joinWaitList(session);
                break;
            case "sdpOffer":
            case "sdpAnswer":
            case "onIceCandidate":
                matchService.transferMessageToOpponent(session, jsonObject);
                break;
            case "stop":
                if (matchService.isAfterMatched(session)) {
                    matchService.transferMessageToOpponent(session, jsonObject);
                    matchService.deCouple(session);
                }
                matchService.withdraw(session);
                break;
            default:
                log.error("Message with Unknown ID received : {}", jsonObject);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        matchService.leave(session);
    }
}
