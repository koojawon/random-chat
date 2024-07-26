package com.rchat.randomChat.match.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionInfoRepository {

    public void put(String id, String opponentId);

    public String getById(String id);

    public void removeId(String id);
}
