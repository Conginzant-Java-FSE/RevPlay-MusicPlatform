package com.revplay.musicplatform.user.repository;

import com.revplay.musicplatform.user.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    long deleteByExpiresAtBeforeOrUsedAtIsNotNull(Instant expiryTime);
}
