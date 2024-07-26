package com.rchat.randomChat.match.repository.impl;

import com.rchat.randomChat.match.repository.WaitQueueRepository;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;

@Component
public class InMemoryWaitQueueRepository implements WaitQueueRepository {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void enqueue(String id) {
        queue.add(id);
    }

    @Override
    public String dequeue() {
        return queue.poll();
    }
}
