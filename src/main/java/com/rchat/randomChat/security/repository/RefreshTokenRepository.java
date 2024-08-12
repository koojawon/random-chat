package com.rchat.randomChat.security.repository;

import com.rchat.randomChat.security.repository.entity.UserRefreshToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<UserRefreshToken, String> {
    Optional<UserRefreshToken> findByEmail(String email);
}
