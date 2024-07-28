package com.rchat.randomChat.match.repository.impl;

import com.rchat.randomChat.match.repository.WaitQueueRepository;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.stereotype.Component;

@Component
public class InMemoryWaitQueueRepository implements WaitQueueRepository {

    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private final ConcurrentSkipListSet<String> withdraws = new ConcurrentSkipListSet<>();

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
        String rval;
        while (true) {
            rval = queue.poll();
            if (withdraws.remove(rval)) {
                continue;
            }
            return rval;
        }
    }

    @Override
    public void remove(String id) {
        withdraws.add(id);
    }
}
