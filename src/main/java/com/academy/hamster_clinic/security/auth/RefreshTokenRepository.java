package com.academy.hamster_clinic.security.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Найти все активные токены в семействе (для отзыва при атаке)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.familyId = :familyId AND rt.revokedAt IS NULL")
    List<RefreshToken> findActiveByFamilyId(@Param("familyId") UUID familyId);

    // Удалить просроченные токены (для планировщика очистки)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    void deleteExpiredBefore(@Param("cutoff") OffsetDateTime cutoff);
}
