package com.example.ChatApp_Internal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long verifiedUsers;
    private long totalFiles;
    private long totalFileSize;
    private ProviderStats providerStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderStats {
        private long localUsers;
        private long googleUsers;
        private long ssoUsers;
    }
}
