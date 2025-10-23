package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.request.UpdateUserRolesRequest;
import com.example.ChatApp_Internal.dto.response.AdminUserResponse;
import com.example.ChatApp_Internal.dto.response.PageResponse;
import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.entity.Profile;
import com.example.ChatApp_Internal.entity.Role;
import com.example.ChatApp_Internal.repository.AccountRepository;
import com.example.ChatApp_Internal.repository.ProfileRepository;
import com.example.ChatApp_Internal.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;

    public PageResponse<AdminUserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Account> accountPage = accountRepository.findAll(pageable);

        List<AdminUserResponse> users = accountPage.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserResponse>builder()
                .content(users)
                .pageNumber(accountPage.getNumber())
                .pageSize(accountPage.getSize())
                .totalElements(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .last(accountPage.isLast())
                .build();
    }

    public AdminUserResponse getUserById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToAdminUserResponse(account);
    }

    public List<AdminUserResponse> searchUsers(String keyword) {
        List<Account> accounts = accountRepository.findAll().stream()
                .filter(account -> {
                    String email = account.getEmail().toLowerCase();
                    String username = account.getProfile() != null ?
                            account.getProfile().getUsername().toLowerCase() : "";
                    String displayName = account.getProfile() != null ?
                            account.getProfile().getDisplayName().toLowerCase() : "";
                    String searchTerm = keyword.toLowerCase();

                    return email.contains(searchTerm) ||
                            username.contains(searchTerm) ||
                            displayName.contains(searchTerm);
                })
                .collect(Collectors.toList());

        return accounts.stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserResponse updateUserRoles(Long accountId, UpdateUserRolesRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get roles from database
        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }

        account.setRoles(roles);
        accountRepository.save(account);

        log.info("Roles updated for user {}: {}", account.getEmail(), request.getRoles());

        return mapToAdminUserResponse(account);
    }

    @Transactional
    public AdminUserResponse activateUser(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        account.setIsActive(true);
        accountRepository.save(account);

        log.info("User activated: {}", account.getEmail());

        return mapToAdminUserResponse(account);
    }

    @Transactional
    public AdminUserResponse deactivateUser(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        account.setIsActive(false);
        accountRepository.save(account);

        log.info("User deactivated: {}", account.getEmail());

        return mapToAdminUserResponse(account);
    }

    @Transactional
    public void deleteUser(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Don't allow deleting your own account
        // This check should be done in controller with current user context

        accountRepository.delete(account);

        log.info("User deleted: {}", account.getEmail());
    }

    @Transactional
    public AdminUserResponse verifyUserEmail(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        account.setIsVerified(true);
        accountRepository.save(account);

        log.info("Email verified for user: {}", account.getEmail());

        return mapToAdminUserResponse(account);
    }

    private AdminUserResponse mapToAdminUserResponse(Account account) {
        Profile profile = account.getProfile();

        return AdminUserResponse.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .username(profile != null ? profile.getUsername() : null)
                .displayName(profile != null ? profile.getDisplayName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .provider(account.getProvider())
                .isVerified(account.getIsVerified())
                .isActive(account.getIsActive())
                .status(profile != null ? profile.getStatus() : null)
                .lastActiveAt(profile != null ? profile.getLastActiveAt() : null)
                .roles(account.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
