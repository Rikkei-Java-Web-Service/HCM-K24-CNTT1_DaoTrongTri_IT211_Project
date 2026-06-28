package com.re.project.service;

import com.re.project.entity.TokenBlacklist;
import com.re.project.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Transactional
    public void addToBlacklist(String token, long expirationInMs) {
        if (expirationInMs > 0) {
            TokenBlacklist blacklist = TokenBlacklist.builder()
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusNanos(expirationInMs * 1_000_000))
                    .build();
            tokenBlacklistRepository.save(blacklist);
        }
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
