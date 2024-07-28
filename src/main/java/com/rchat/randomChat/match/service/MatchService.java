package com.rchat.randomChat.match.service;

import com.google.gson.JsonObject;
import com.rchat.randomChat.match.repository.ConnectionInfoRepository;
import com.rchat.randomChat.match.repository.WaitQueueRepository;
import com.rchat.randomChat.websocket.repository.WebSocketSessionRepository;
import com.rchat.randomChat.websocket.service.SendService;
import com.rchat.randomChat.websocket.statics.SdpGenerationOrderMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final WebSocketSessionRepository sessionRepository;

    private final ConnectionInfoRepository connectionInfoRepository;
    private final WaitQueueRepository queueRepository;

    private final SendService sendService;

    public void joinWaitList(WebSocketSession session) throws IOException {
        String opponentId;
        while (true) {
            if (queueRepository.isEmpty()) {
                queueRepository.enqueue(session.getId());
                return;
            }
            opponentId = queueRepository.dequeue();
            if (!checkAliveById(opponentId)) {
                queueRepository.remove(opponentId);
                continue;
            }
            break;
        }
        matchSuccess(session, opponentId);
    }

    private void matchSuccess(WebSocketSession session, String opponentId) throws IOException {
        connectionInfoRepository.put(session.getId(), opponentId);
        sendService.sendMessage(session, new SdpGenerationOrderMessage());
    }

    public void deCouple(WebSocketSession session) {
        connectionInfoRepository.removeId(session.getId());
    }

    public void withdraw(WebSocketSession session) {
        queueRepository.remove(session.getId());
    }

    public void transferMessageToOpponent(WebSocketSession session, JsonObject jsonObject) throws IOException {
        String opponentId = connectionInfoRepository.getById(session.getId());
        WebSocketSession oppoSession = sessionRepository.get(opponentId);
        sendService.sendMessage(oppoSession, jsonObject);
    }

    public boolean isAfterMatched(WebSocketSession session) {
        if (connectionInfoRepository.getById(session.getId()) == null) {
            withdraw(session);
            return false;
        }
        return true;
    }

    public void join(WebSocketSession session) {
        sessionRepository.put(session);
    }

    public void leave(WebSocketSession session) {
        sessionRepository.remove(session);
    }

    private boolean checkAliveById(String sessionId) {
        return sessionRepository.get(sessionId).isOpen();
    }
}
