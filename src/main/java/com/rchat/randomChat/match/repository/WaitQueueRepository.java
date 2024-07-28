package com.rchat.randomChat.match.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface WaitQueueRepository {

    public boolean isEmpty();

    public void enqueue(String id);

    public String dequeue();

    public void remove(String id);
}
