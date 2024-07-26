package com.rchat.randomChat.match.service;

import com.google.gson.JsonObject;
import com.rchat.randomChat.match.repository.ConnectionInfoRepository;
import com.rchat.randomChat.match.repository.WaitQueueRepository;
import com.rchat.randomChat.match.repository.WithDrawalRepository;
import com.rchat.randomChat.websocket.service.SendService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final ConnectionInfoRepository connectionInfoRepository;
    private final WaitQueueRepository queueRepository;
    private final WithDrawalRepository withDrawalRepository;

    private final SendService sendService;

    public void joinWaitList(WebSocketSession session) throws IOException {
        String opponentId;
        while (true) {
            opponentId = queueRepository.dequeue();
            if (opponentId == null) {
                queueRepository.enqueue(session.getId());
                return;
            }
            if (sendService.checkAlive(opponentId)) {
                withDrawalRepository.put(opponentId);
                continue;
            }
            if (!withDrawalRepository.contains(opponentId)) {
                break;
            }
            withDrawalRepository.remove(opponentId);
        }
        matchSuccess(session.getId(), opponentId);
    }

    private void matchSuccess(String id, String opponentId) throws IOException {
        connectionInfoRepository.put(id, opponentId);
        sendService.orderGenerateOffer(id);
    }

    public void deCouple(WebSocketSession session) {
        connectionInfoRepository.removeId(session.getId());
    }

    public void onIceCandidate(WebSocketSession session, JsonObject jsonObject) throws IOException {
        String opponentId = connectionInfoRepository.getById(session.getId());
        sendService.sendIceCandidate(opponentId, jsonObject);
    }

    public void withdraw(WebSocketSession session) {
        withDrawalRepository.put(session.getId());
    }

    public void sendSdpToOpponent(WebSocketSession session, JsonObject jsonObject) throws IOException {
        String opponentId = connectionInfoRepository.getById(session.getId());
        sendService.sendSdp(opponentId, jsonObject);
    }

    public void sendStopToOpponent(WebSocketSession session, JsonObject jsonObject) throws IOException {
        String opponentId = connectionInfoRepository.getById(session.getId());
        sendService.sendStop(opponentId, jsonObject);
    }

    public boolean isAfterMatched(WebSocketSession session) {
        if (connectionInfoRepository.getById(session.getId()) == null) {
            withdraw(session);
            return false;
        }
        return true;
    }
}
