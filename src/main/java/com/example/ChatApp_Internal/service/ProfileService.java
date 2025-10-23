package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.request.ChangePasswordRequest;
import com.example.ChatApp_Internal.dto.request.UpdateProfileRequest;
import com.example.ChatApp_Internal.dto.request.UpdateStatusRequest;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.entity.Profile;
import com.example.ChatApp_Internal.entity.UserStatus;
import com.example.ChatApp_Internal.repository.AccountRepository;
import com.example.ChatApp_Internal.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AwsS3Service awsS3Service;

    public UserInfo getCurrentUserInfo() {
        Account account = getCurrentAccount();
        Profile profile = account.getProfile();

        return mapToUserInfo(account, profile);
    }

    @Transactional
    public UserInfo updateProfile(UpdateProfileRequest request) {
        Account account = getCurrentAccount();
        Profile profile = account.getProfile();

        if (profile == null) {
            throw new RuntimeException("Profile not found");
        }

        // Update username if provided and different
        if (request.getUsername() != null && !request.getUsername().equals(profile.getUsername())) {
            if (profileRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            profile.setUsername(request.getUsername());
        }

        // Update display name
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }

        // Update bio
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        profileRepository.save(profile);

        log.info("Profile updated for user: {}", account.getEmail());

        return mapToUserInfo(account, profile);
    }

    @Transactional
    public UserInfo updateAvatar(MultipartFile file) {
        Account account = getCurrentAccount();
        Profile profile = account.getProfile();

        if (profile == null) {
            throw new RuntimeException("Profile not found");
        }

        // Upload to S3
        var fileMetadata = awsS3Service.uploadFile(file, "avatars");

        // Update profile avatar URL
        profile.setAvatarUrl(fileMetadata.getUrl());
        profileRepository.save(profile);

        log.info("Avatar updated for user: {}", account.getEmail());

        return mapToUserInfo(account, profile);
    }

    @Transactional
    public UserInfo updateStatus(UpdateStatusRequest request) {
        Account account = getCurrentAccount();
        Profile profile = account.getProfile();

        if (profile == null) {
            throw new RuntimeException("Profile not found");
        }

        profile.setStatus(request.getStatus());
        profile.setLastActiveAt(System.currentTimeMillis());
        profileRepository.save(profile);

        log.info("Status updated for user: {} to {}", account.getEmail(), request.getStatus());

        return mapToUserInfo(account, profile);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Account account = getCurrentAccount();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        log.info("Password changed for user: {}", account.getEmail());
    }

    @Transactional
    public void deleteAccount() {
        Account account = getCurrentAccount();

        // Soft delete - deactivate account
        account.setIsActive(false);
        accountRepository.save(account);

        log.info("Account deactivated for user: {}", account.getEmail());
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private UserInfo mapToUserInfo(Account account, Profile profile) {
        return UserInfo.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .username(profile != null ? profile.getUsername() : null)
                .displayName(profile != null ? profile.getDisplayName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .status(profile != null ? profile.getStatus() : UserStatus.OFFLINE)
                .roles(account.getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet()))
                .isVerified(account.getIsVerified())
                .build();
    }
}