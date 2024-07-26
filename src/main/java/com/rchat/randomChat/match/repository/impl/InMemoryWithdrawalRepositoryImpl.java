package com.rchat.randomChat.match.repository.impl;

import com.rchat.randomChat.match.repository.WithDrawalRepository;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.stereotype.Component;

@Component
public class InMemoryWithdrawalRepositoryImpl implements WithDrawalRepository {

    private final ConcurrentSkipListSet<String> set = new ConcurrentSkipListSet<>();

    @Override
    public void put(String id) {
        set.add(id);
    }

    @Override
    public boolean contains(String id) {
        return set.contains(id);
    }

    @Override
    public boolean remove(String id) {
        return set.remove(id);
    }
}
