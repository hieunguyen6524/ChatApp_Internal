package com.example.ChatApp_Internal.service;


import com.example.ChatApp_Internal.dto.request.AddMemberRequest;
import com.example.ChatApp_Internal.dto.request.CreateConversationRequest;
import com.example.ChatApp_Internal.dto.response.ConversationResponse;
import com.example.ChatApp_Internal.dto.response.MemberResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        Account currentAccount = getCurrentAccount();

        if (!workspaceMemberRepository
                .existsByWorkspaceWorkspaceIdAndAccountAccountId(request.getWorkspaceId(), currentAccount.getAccountId())) {
            throw new RuntimeException("You are not a member of this workspace");
        }

        Workspace workspace = workspaceRepository
                .findById(request.getWorkspaceId())
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Conversation conversation = Conversation.builder()
                .workspace(workspace)
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .isPrivate(request.getIsPrivate())
                .isArchived(false)
                .createdBy(currentAccount)
                .build();

        conversation = conversationRepository.save(conversation);

        ConversationMember creator = ConversationMember.builder()
                .conversation(conversation)
                .account(currentAccount)
                .isChannelAdmin(true)
                .isNotifEnabled(true)
                .build();

        conversationMemberRepository.save(creator);

        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(currentAccount.getAccountId())) {
                    Account member = accountRepository
                            .findById(memberId)
                            .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));
                    verifyWorkspaceMembership(workspace.getWorkspaceId(), member.getAccountId(), "That member is not currently in the workspace.");
                    ConversationMember convoMember = ConversationMember.builder()
                            .conversation(conversation)
                            .account(member)
                            .isChannelAdmin(false)
                            .isNotifEnabled(true)
                            .build();

                    conversationMemberRepository.save(convoMember);

                }
            }
        }
        log.info("Conversation created: {} in workspace {}", conversation.getName(), workspace.getName());

        return mapToConversationResponse(conversation, currentAccount);
    }

    public List<ConversationResponse> getWorkspaceConversations(Long workspaceId) {
        Account currentAccount = getCurrentAccount();
        verifyWorkspaceMembership(workspaceId, currentAccount.getAccountId(), "");

        /*
         * Chi lay nhung conversation ma user da tham gia public / private
         * Dang bi loi o cho member da xoa nhung van get duoc conversation do (xong)
         * */

        List<Conversation> conversations = conversationRepository
                .findByWorkspaceIdAndMemberAccountId(workspaceId, currentAccount.getAccountId());

        return conversations.stream()
                .map(conv -> mapToConversationResponse(conv, currentAccount))
                .collect(Collectors.toList());
    }

    public ConversationResponse getConversationById(Long conversationId) {
        Account currentAccount = getCurrentAccount();

        verifyConversationMembership(conversationId, currentAccount.getAccountId());
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));


        return mapToConversationResponse(conversation, currentAccount);
    }

    public List<MemberResponse> getConversationMembers(Long conversationId) {
        Account currentAccount = getCurrentAccount();

        verifyConversationMembership(conversationId, currentAccount.getAccountId());

        List<ConversationMember> members = conversationMemberRepository
                .findByConversationConversationId(conversationId);

        return members.stream()
                .map(this::mapToConversationMemberResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse addConversationMember(Long conversationId, AddMemberRequest request) {
        Account currentAccount = getCurrentAccount();

        verifyConversationAdmin(conversationId, currentAccount.getAccountId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        Account newMember = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!workspaceMemberRepository.existsByWorkspaceWorkspaceIdAndAccountAccountId(conversation.getWorkspace().getWorkspaceId(), request.getAccountId())) {
            throw new RuntimeException("That user is not currently in the workspace.");
        }

        ConversationMember existingMember = conversationMemberRepository
                .findByConversationConversationIdAndAccountAccountId(conversationId, newMember.getAccountId())
                .orElse(null);

        if (existingMember != null) {
            if (existingMember.getIsActive()) {
                throw new RuntimeException("User is already a member");
            }

            existingMember.setIsActive(true);
            conversationMemberRepository.save(existingMember);

            log.info("Member added to conversation {}: {}", conversation.getName(), newMember.getEmail());
            return mapToConversationMemberResponse(existingMember);
        }

        ConversationMember member = ConversationMember.builder()
                .conversation(conversation)
                .account(newMember)
                .isActive(true)
                .isChannelAdmin(false)
                .isNotifEnabled(true)
                .build();

        conversationMemberRepository.save(member);
        log.info("Member added to conversation {}: {}", conversation.getName(), newMember.getEmail());

        return mapToConversationMemberResponse(member);
    }

    @Transactional
    public ConversationResponse updateConversation(Long conversationId, CreateConversationRequest request) {
        Account currentAccount = getCurrentAccount();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        verifyConversationAdmin(conversation.getConversationId(), currentAccount.getAccountId());


        if (request.getName() != null && !request.getName().isBlank()) {
            conversation.setName(request.getName());
        }

        if (request.getDescription() != null) {
            conversation.setDescription(request.getDescription());
        }

        if (request.getIsPrivate() != null) {
            conversation.setIsPrivate(request.getIsPrivate());
        }

        conversationRepository.save(conversation);

        return mapToConversationResponse(conversation, currentAccount);
    }


    @Transactional
    public void removeMember(Long conversationId, Long memberId) {
        Account currentAccount = getCurrentAccount();

        verifyConversationAdmin(conversationId, currentAccount.getAccountId());

        ConversationMember member = conversationMemberRepository
                .findByConversationConversationIdAndAccountAccountId(conversationId, memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setIsActive(false);
        member.setLeftAt(System.currentTimeMillis());
        conversationMemberRepository.save(member);

        log.info("Member removed from conversation: {}", member.getAccount().getEmail());
    }

    @Transactional
    public void leaveConversation(Long conversationId) {
        Account currentAccount = getCurrentAccount();

        ConversationMember member = conversationMemberRepository
                .findByConversationConversationIdAndAccountAccountId(conversationId, currentAccount.getAccountId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this conversation"));

        member.setIsActive(false);
        member.setLeftAt(System.currentTimeMillis());
        conversationMemberRepository.save(member);

        log.info("User left conversation: {}", currentAccount.getEmail());
    }

    private void verifyWorkspaceMembership(Long workspaceId, Long accountId, String message) {
        if (!workspaceMemberRepository.existsByWorkspaceWorkspaceIdAndAccountAccountId(
                workspaceId, accountId)) {
            throw new RuntimeException(!message.isEmpty() ? message : "You are not a member of this workspace");
        }
    }

    private void verifyConversationMembership(Long conversationId, Long accountId) {
        if (!conversationMemberRepository.existsByConversationConversationIdAndAccountAccountIdAndIsActiveTrue(
                conversationId, accountId)) {
            throw new RuntimeException("You are not a member of this conversation");
        }
    }

    private void verifyConversationAdmin(Long conversationId, Long accountId) {
        ConversationMember member = conversationMemberRepository
                .findByConversationConversationIdAndAccountAccountId(conversationId, accountId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this conversation"));

        if (!member.getIsChannelAdmin()) {
            throw new RuntimeException("You don't have admin permission");
        }
    }


    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private ConversationResponse mapToConversationResponse(Conversation conversation, Account currentAccount) {
        ConversationMember currentMember = conversationMemberRepository
                .findByConversationConversationIdAndAccountAccountId(
                        conversation.getConversationId(), currentAccount.getAccountId())
                .orElse(null);

        Long unreadCount = 0L;
        if (currentMember != null && currentMember.getLastReadAt() != null) {
            unreadCount = messageRepository.countByConversationConversationIdAndCreatedAtGreaterThan(
                    conversation.getConversationId(), currentMember.getLastReadAt());
        }

        return ConversationResponse.builder()
                .conversationId(conversation.getConversationId())
                .workspaceId(conversation.getWorkspace().getWorkspaceId())
                .name(conversation.getName())
                .description(conversation.getDescription())
                .type(conversation.getType())
                .isPrivate(conversation.getIsPrivate())
                .isArchived(conversation.getIsArchived())
                .createdBy(mapToUserInfo(conversation.getCreatedBy()))
                .memberCount((long) conversation.getMembers().size())
                .unreadCount(unreadCount)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private MemberResponse mapToConversationMemberResponse(ConversationMember member) {
        return MemberResponse.builder()
                .memberId(member.getId())
                .user(mapToUserInfo(member.getAccount()))
                .role(member.getIsChannelAdmin() ? "ADMIN" : "MEMBER")
                .isActive(member.getIsActive())
                .joinedAt(member.getJoinedAt())
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
}
