package com.example.ChatApp_Internal.dto.response;

import com.example.ChatApp_Internal.entity.AuthProvider;
import com.example.ChatApp_Internal.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {
    private Long accountId;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;
    private AuthProvider provider;
    private Boolean isVerified;
    private Boolean isActive;
    private UserStatus status;
    private Long lastActiveAt;
    private Set<String> roles;
    private Long createdAt;
    private Long updatedAt;
}
