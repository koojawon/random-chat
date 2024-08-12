package com.rchat.randomChat.global.repository;

import com.rchat.randomChat.global.repository.entity.UserInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByEmail(String email);

    boolean existsByEmail(String email);
}
