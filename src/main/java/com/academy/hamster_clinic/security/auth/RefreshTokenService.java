package com.academy.hamster_clinic.security.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.model.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    @Transactional
    public String createRefreshToken(User user, UUID familyId) {
        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setFamilyId(familyId != null ? familyId : UUID.randomUUID());
        refreshToken.setCreatedAt(OffsetDateTime.now());
        refreshToken.setExpiresAt(OffsetDateTime.now().plus(REFRESH_TOKEN_TTL));

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }


    @Transactional
    public RefreshResult rotateRefreshToken(String rawToken) {
        String tokenHash = sha256(rawToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Недействительный refresh-токен"));
        
                if (storedToken.getRevokedAt() != null) {
            revokeTokenFamily(storedToken.getFamilyId());
            throw new BusinessException("Обнаружено повторное использование токена. Все сессии отозваны.");
        }

        if (storedToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("Срок действия refresh-токена истёк");
        }
        
        storedToken.revoke();
        storedToken.setReplacedBy(sha256(generateRawToken()));

        User user = storedToken.getUser();
        String newRawToken = createRefreshToken(user, storedToken.getFamilyId());

        return new RefreshResult(newRawToken, user);
    }

    @Transactional
    public void revokeTokenFamily(UUID familyId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findActiveByFamilyId(familyId);
        activeTokens.forEach(RefreshToken::revoke);
    }


    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public record RefreshResult(String newRefreshToken, User user) {}
}
