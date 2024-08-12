package com.rchat.randomChat.member.repository;

import com.rchat.randomChat.member.repository.entity.UserInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {

    Optional<UserInfo> findByEmail(String email);

    boolean existsByEmail(String email);
}
