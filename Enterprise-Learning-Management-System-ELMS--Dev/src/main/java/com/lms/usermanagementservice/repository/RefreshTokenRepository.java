package com.lms.usermanagementservice.repository;

import com.lms.usermanagementservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    boolean existsByToken(String token);

    void deleteAllByUserId(Long userId);

    List<RefreshToken> findByUserId(Long userId);

    default List<RefreshToken> findAllByUserId(Long userId) {
        return findByUserId(userId);
    }

    List<RefreshToken> findAllByExpiresAtBefore(LocalDateTime expiresAt);

    default List<RefreshToken> findAllByExpiryDateBefore(LocalDateTime expiryDate) {
        return findAllByExpiresAtBefore(expiryDate);
    }
}
