package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.UpdateUserRolesRequest;
import com.example.ChatApp_Internal.dto.response.AdminUserResponse;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.PageResponse;
import com.example.ChatApp_Internal.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<AdminUserResponse> users = adminService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable Long accountId) {
        AdminUserResponse user = adminService.getUserById(accountId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> searchUsers(
            @RequestParam String keyword) {
        List<AdminUserResponse> users = adminService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PatchMapping("/{accountId}/roles")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateUserRoles(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        AdminUserResponse user = adminService.updateUserRoles(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", user));
    }

    @PatchMapping("/{accountId}/activate")
    public ResponseEntity<ApiResponse<AdminUserResponse>> activateUser(@PathVariable Long accountId) {
        AdminUserResponse user = adminService.activateUser(accountId);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", user));
    }

    @PatchMapping("/{accountId}/deactivate")
    public ResponseEntity<ApiResponse<AdminUserResponse>> deactivateUser(@PathVariable Long accountId) {
        AdminUserResponse user = adminService.deactivateUser(accountId);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", user));
    }

    @PatchMapping("/{accountId}/verify")
    public ResponseEntity<ApiResponse<AdminUserResponse>> verifyUserEmail(@PathVariable Long accountId) {
        AdminUserResponse user = adminService.verifyUserEmail(accountId);
        return ResponseEntity.ok(ApiResponse.success("User email verified successfully", user));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long accountId) {
        adminService.deleteUser(accountId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
