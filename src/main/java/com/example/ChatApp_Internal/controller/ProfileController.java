package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.ChangePasswordRequest;
import com.example.ChatApp_Internal.dto.request.UpdateProfileRequest;
import com.example.ChatApp_Internal.dto.request.UpdateStatusRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser() {
        UserInfo userInfo = profileService.getCurrentUserInfo();
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserInfo>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserInfo userInfo = profileService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userInfo));
    }

    @PatchMapping("/avatar")
    public ResponseEntity<ApiResponse<UserInfo>> updateAvatar(
            @RequestParam("file") MultipartFile file) {
        UserInfo userInfo = profileService.updateAvatar(file);
        return ResponseEntity.ok(ApiResponse.success("Avatar updated successfully", userInfo));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<UserInfo>> updateStatus(
            @Valid @RequestBody UpdateStatusRequest request) {
        UserInfo userInfo = profileService.updateStatus(request);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", userInfo));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        profileService.deleteAccount();
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}

