package com.rchat.randomChat.match.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface WithDrawalRepository {

    public void put(String id);

    public boolean contains(String id);

    public boolean remove(String id);
}
