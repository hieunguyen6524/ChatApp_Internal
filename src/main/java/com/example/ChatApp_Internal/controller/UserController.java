package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.entity.Profile;
import com.example.ChatApp_Internal.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AccountRepository accountRepository;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = account.getProfile();

        UserInfo userInfo = UserInfo.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .username(profile != null ? profile.getUsername() : null)
                .displayName(profile != null ? profile.getDisplayName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .status(profile != null ? profile.getStatus() : null)
                .roles(account.getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet()))
                .isVerified(account.getIsVerified())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInfo>> getUserById(@PathVariable Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = account.getProfile();

        UserInfo userInfo = UserInfo.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .username(profile != null ? profile.getUsername() : null)
                .displayName(profile != null ? profile.getDisplayName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .status(profile != null ? profile.getStatus() : null)
                .roles(account.getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet()))
                .isVerified(account.getIsVerified())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}