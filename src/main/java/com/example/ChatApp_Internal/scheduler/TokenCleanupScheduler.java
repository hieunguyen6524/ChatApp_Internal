package com.example.ChatApp_Internal.scheduler;

import com.example.ChatApp_Internal.repository.BlacklistedTokenRepository;
import com.example.ChatApp_Internal.repository.RefreshContextRepository;
import com.example.ChatApp_Internal.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshContextRepository refreshContextRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup job");

        long currentTime = System.currentTimeMillis();

        try {
            refreshContextRepository.deleteExpiredTokens(currentTime);
            log.info("Deleted expired refresh contexts");

            blacklistedTokenRepository.deleteExpiredTokens(currentTime);
            log.info("Deleted expired blacklisted tokens");

            verificationTokenRepository.deleteExpiredTokens(currentTime);
            log.info("Deleted expired verification tokens");

        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }

        log.info("Token cleanup job completed");
    }
}