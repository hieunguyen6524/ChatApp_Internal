package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.response.SystemStatsResponse;
import com.example.ChatApp_Internal.entity.AuthProvider;
import com.example.ChatApp_Internal.repository.AccountRepository;
import com.example.ChatApp_Internal.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final AccountRepository accountRepository;
    private final FileMetadataRepository fileMetadataRepository;

    public SystemStatsResponse getSystemStats() {
        long totalUsers = accountRepository.count();
        long activeUsers = accountRepository.countActiveAccounts();
        long verifiedUsers = accountRepository.findAll().stream()
                .filter(account -> account.getIsVerified())
                .count();

        long totalFiles = fileMetadataRepository.count();
        Long totalFileSize = fileMetadataRepository.findAll().stream()
                .filter(file -> !file.getIsDeleted())
                .mapToLong(file -> file.getFileSize())
                .sum();

        long localUsers = accountRepository.countByProvider(AuthProvider.LOCAL);
        long googleUsers = accountRepository.countByProvider(AuthProvider.GOOGLE);
        long ssoUsers = accountRepository.countByProvider(AuthProvider.SSO);

        SystemStatsResponse.ProviderStats providerStats =
                SystemStatsResponse.ProviderStats.builder()
                        .localUsers(localUsers)
                        .googleUsers(googleUsers)
                        .ssoUsers(ssoUsers)
                        .build();

        return SystemStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .verifiedUsers(verifiedUsers)
                .totalFiles(totalFiles)
                .totalFileSize(totalFileSize)
                .providerStats(providerStats)
                .build();
    }
}
