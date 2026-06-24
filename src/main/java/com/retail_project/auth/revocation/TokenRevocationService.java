package com.retail_project.auth.revocation;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private static final Logger log = LoggerFactory.getLogger(TokenRevocationService.class);

    private final RevokedTokenRepository revokedTokenRepository;

    public void revoke(String token) {
        String hash = sha256(token);
        if (!revokedTokenRepository.existsByTokenHash(hash)) {
            revokedTokenRepository.save(new RevokedToken(hash));
        }
    }

    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsByTokenHash(sha256(token));
    }

    // Runs nightly at 03:00 — removes tokens older than the refresh token lifetime (7 days)
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpired() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        revokedTokenRepository.deleteExpiredBefore(cutoff);
        log.info("Purged revoked tokens older than {}", cutoff);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
