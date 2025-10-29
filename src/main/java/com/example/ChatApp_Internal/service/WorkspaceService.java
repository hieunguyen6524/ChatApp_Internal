package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.request.AddMemberRequest;
import com.example.ChatApp_Internal.dto.request.CreateWorkspaceRequest;
import com.example.ChatApp_Internal.dto.response.MemberResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.dto.response.WorkspaceResponse;
import com.example.ChatApp_Internal.entity.*;
import com.example.ChatApp_Internal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final AccountRepository accountRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        Account currentAccount = getCurrentAccount();

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentAccount)
                .isArchived(false)
                .build();

        workspaceRepository.save(workspace);

        WorkspaceRole adminRole = workspaceRoleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .account(currentAccount)
                .role(adminRole)
                .isActive(true)
                .build();

        workspaceMemberRepository.save(member);

        log.info("Workspace created: {} by {}", workspace.getName(), currentAccount.getEmail());

        return mapToWorkspaceResponse(workspace, currentAccount);
    }

    public List<WorkspaceResponse> getMyWorkspaces() {
        Account currentAccount = getCurrentAccount();

        List<Workspace> workspaces = workspaceRepository.findByMemberAccountId(currentAccount.getAccountId());

        return workspaces.stream()
                .map(ws -> mapToWorkspaceResponse(ws, currentAccount)).collect(Collectors.toList());
    }

    public WorkspaceResponse getWorkspaceById(Long workspaceId) {
        Account currentAccount = getCurrentAccount();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        verifyMembership(workspaceId, currentAccount.getAccountId());
        return mapToWorkspaceResponse(workspace, currentAccount);
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, CreateWorkspaceRequest request) {
        Account currentAccount = getCurrentAccount();
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        verifyWorkspaceAdmin(workspaceId, currentAccount.getAccountId());

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            workspace.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workspace.setDescription(request.getDescription());
        }
        workspaceRepository.save(workspace);

        log.info("Workspace updated: {}", workspace.getName());

        return mapToWorkspaceResponse(workspace, currentAccount);

    }

    @Transactional
    public MemberResponse addMember(Long workspaceId, AddMemberRequest request) {
        Account currentAccount = getCurrentAccount();

        verifyWorkspaceAdmin(workspaceId, currentAccount.getAccountId());

        Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(() -> new RuntimeException("Workspace not found"));

        Account newMember = accountRepository.findById(request.getAccountId()).orElseThrow(() -> new RuntimeException("User not found"));
        WorkspaceRole role = workspaceRoleRepository
                .findByRoleName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        WorkspaceMember existingMember = workspaceMemberRepository
                .findByWorkspaceWorkspaceIdAndAccountAccountId(workspaceId, newMember.getAccountId())
                .orElse(null);

        if (existingMember != null) {
            if (existingMember.getIsActive()) {
                throw new RuntimeException("User is already a member");
            }
            existingMember.setIsActive(true);
            existingMember.setRole(role);
            workspaceMemberRepository.save(existingMember);
            log.info("Reactivated member {} in workspace {}", newMember.getEmail(), workspace.getName());
            return mapToMemberResponse(existingMember);
        }


        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .account(newMember)
                .role(role)
                .isActive(true)
                .build();

        workspaceMemberRepository.save(member);

        log.info("Member added to workspace {}: {}", workspace.getName(), newMember.getEmail());

        return mapToMemberResponse(member);
    }

    public List<MemberResponse> getWorkspaceMembers(Long workspaceId) {
        Account currentAccount = getCurrentAccount();

        verifyMembership(workspaceId, currentAccount.getAccountId());

        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceWorkspaceIdAndIsActiveTrue(workspaceId);

        return members.stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(Long workspaceId, Long memberId) {
        Account currentAccount = getCurrentAccount();

        verifyWorkspaceAdmin(workspaceId, currentAccount.getAccountId());

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceWorkspaceIdAndAccountAccountId(workspaceId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setIsActive(false);
        workspaceMemberRepository.save(member);

        List<ConversationMember> conversations = conversationMemberRepository
                .findActiveMembersInWorkspaceByAccountId(workspaceId, memberId);

        for (ConversationMember conv : conversations) {
            conv.setIsActive(false);
            conv.setLeftAt(System.currentTimeMillis());
        }

        if (!conversations.isEmpty()) {
            conversationMemberRepository.saveAll(conversations);
            log.info("Removed {} conversation memberships for member {}", conversations.size(), memberId);
        }


        log.info("Member removed from workspace: {}", member.getAccount().getEmail());
    }

    @Transactional
    public void leaveWorkspace(Long workspaceId) {
        Account currentAccount = getCurrentAccount();

        verifyMembership(workspaceId, currentAccount.getAccountId());

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceWorkspaceIdAndAccountAccountId(workspaceId, currentAccount.getAccountId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this workspace"));

        member.setIsActive(false);
        workspaceMemberRepository.save(member);
        List<ConversationMember> conversations = conversationMemberRepository
                .findActiveMembersInWorkspaceByAccountId(workspaceId, currentAccount.getAccountId());

        for (ConversationMember conv : conversations) {
            conv.setIsActive(false);
            conv.setLeftAt(System.currentTimeMillis());
        }

        if (!conversations.isEmpty()) {
            conversationMemberRepository.saveAll(conversations);
            log.info("Leave {} conversation memberships for member {}", conversations.size(), currentAccount.getAccountId());
        }
        log.info("User left workspace: {}", currentAccount.getEmail());
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return accountRepository.findByEmail(auth.getName()).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private WorkspaceResponse mapToWorkspaceResponse(Workspace workspace, Account currentAccount) {
        WorkspaceMember currentMember = workspaceMemberRepository
                .findByWorkspaceWorkspaceIdAndAccountAccountId(workspace.getWorkspaceId(), currentAccount.getAccountId())
                .orElse(null);

        return WorkspaceResponse.builder()
                .workspaceId(workspace.getWorkspaceId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .createdBy(mapToUserInfo(workspace.getCreatedBy()))
                .isArchived(workspace.getIsArchived())
                .memberCount((long) workspace.getMembers().size())
                .userRole(currentMember != null ? currentMember.getRole().getRoleName() : null)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }


    private UserInfo mapToUserInfo(Account account) {
        Profile profile = account.getProfile();
        return UserInfo.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .username(profile != null ? profile.getUsername() : null)
                .displayName(profile != null ? profile.getDisplayName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .status(profile != null ? profile.getStatus() : null)
                .build();
    }

    private MemberResponse mapToMemberResponse(WorkspaceMember member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .user(mapToUserInfo(member.getAccount()))
                .role(member.getRole().getRoleName())
                .isActive(member.getIsActive())
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private void verifyMembership(Long workspaceId, Long accountId) {
        if (!workspaceMemberRepository.existsByWorkspaceWorkspaceIdAndAccountAccountId(workspaceId, accountId)) {
            throw new RuntimeException("You are not a member of this workspace");
        }
    }

    private void verifyWorkspaceAdmin(Long workspaceId, Long accountId) {
        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceWorkspaceIdAndAccountAccountId(workspaceId, accountId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this workspace"));

        if (!"ADMIN".equals(member.getRole().getRoleName())) {
            throw new RuntimeException("You don't have admin permission");
        }
    }
}
