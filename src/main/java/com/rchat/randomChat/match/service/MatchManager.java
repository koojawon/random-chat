package com.rchat.randomChat.match.service;

import com.rchat.randomChat.match.repository.ConnectionInfoRepository;
import com.rchat.randomChat.match.repository.WaitQueueRepository;
import com.rchat.randomChat.websocket.service.WebsocketManager;
import com.rchat.randomChat.websocket.statics.SdpGenerationOrderMessage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class MatchManager {

    private final ConnectionInfoRepository connectionInfoRepository;
    private final WaitQueueRepository queueRepository;

    private final WebsocketManager websocketManager;

    public void joinQueue(WebSocketSession session) throws IOException {
        String opponentId;
        while (true) {
            if (queueRepository.isEmpty()) {
                queueRepository.enqueue(session.getId());
                return;
            }
            opponentId = queueRepository.dequeue();
            if (!websocketManager.checkAliveById(opponentId)) {
                queueRepository.remove(opponentId);
                continue;
            }
            break;
        }
        matchSuccess(session, opponentId);
    }

    private void matchSuccess(WebSocketSession session, String opponentId) throws IOException {
        connectionInfoRepository.put(session.getId(), opponentId);
        websocketManager.sendMessage(session.getId(), new TextMessage(new SdpGenerationOrderMessage().toString()));
    }

    public void deCouple(WebSocketSession session) {
        connectionInfoRepository.removeId(session.getId());
    }

    public void withdraw(WebSocketSession session) {
        queueRepository.remove(session.getId());
    }

    public String getOpponentId(WebSocketSession session) {
        return connectionInfoRepository.getById(session.getId());
    }

    public boolean isAfterMatched(WebSocketSession session) {
        if (connectionInfoRepository.getById(session.getId()) == null) {
            withdraw(session);
            return false;
        }
        return true;
    }


}
