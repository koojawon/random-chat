package com.rchat.randomChat.match.repository.impl;

import com.rchat.randomChat.match.repository.ConnectionInfoRepository;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;


@Component
public class InMemoryConnectionInfoRepository implements ConnectionInfoRepository {

    private final ConcurrentHashMap<String, String> connectionInfoMap = new ConcurrentHashMap<>();

    @Override
    public void put(String id, String opponentId) {
        connectionInfoMap.put(id, opponentId);
        connectionInfoMap.put(opponentId, id);
    }

    @Override
    public String getById(String id) {
        return connectionInfoMap.get(id);
    }

    @Override
    public void removeId(String id) {
        String opponentId = getById(id);
        connectionInfoMap.computeIfPresent(id, (k, v) -> connectionInfoMap.remove(k));
        connectionInfoMap.computeIfPresent(opponentId, (k, v) -> connectionInfoMap.remove(k));
    }
}
