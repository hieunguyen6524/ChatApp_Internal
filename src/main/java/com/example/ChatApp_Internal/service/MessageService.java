package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.request.SendMessageRequest;
import com.example.ChatApp_Internal.dto.response.MessageResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.entity.Account;
import com.example.ChatApp_Internal.entity.Conversation;
import com.example.ChatApp_Internal.entity.Message;
import com.example.ChatApp_Internal.entity.Profile;
import com.example.ChatApp_Internal.repository.AccountRepository;
import com.example.ChatApp_Internal.repository.ConversationMemberRepository;
import com.example.ChatApp_Internal.repository.ConversationRepository;
import com.example.ChatApp_Internal.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {
    private final AccountRepository accountRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {

        Account currentAccount = getCurrentAccount();

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversationMemberRepository.existsByConversationConversationIdAndAccountAccountIdAndIsActiveTrue(
                request.getConversationId(), currentAccount.getAccountId()
        )) {
            throw new RuntimeException("You are not a member of this conversation");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentAccount)
                .content(request.getContent())
                .contentType(request.getContentType())
                .isDeleted(false)
                .isPinned(false)
                .build();

        if (request.getParentId() != null) {
            Message parent = messageRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent message not found"));
            message.setParent(parent);
        }


        /*
         * Cac chuc nang chua phat trien
         * gui file/link => request.getAttacmentIds()
         * cam xuc tinh nhan
         * */

        message = messageRepository.save((message));

        MessageResponse messageResponse = mapToMessageResponse(message);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                messageResponse
        );

        return messageResponse;
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, Principal principal) {

        Account currentAccount = accountRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Account not found for email: " + principal.getName()));

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversationMemberRepository.existsByConversationConversationIdAndAccountAccountIdAndIsActiveTrue(
                request.getConversationId(), currentAccount.getAccountId()
        )) {
            throw new RuntimeException("You are not a member of this conversation");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentAccount)
                .content(request.getContent())
                .contentType(request.getContentType())
                .isDeleted(false)
                .isPinned(false)
                .build();

        if (request.getParentId() != null) {
            Message parent = messageRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent message not found"));
            message.setParent(parent);
        }


        /*
         * Cac chuc nang chua phat trien
         * gui file/link => request.getAttacmentIds()
         * cam xuc tinh nhan
         * */

        message = messageRepository.save((message));

        MessageResponse messageResponse = mapToMessageResponse(message);

        messagingTemplate.convertAndSend(
                "/topic/conversations/" + request.getConversationId(),
                messageResponse
        );

        return messageResponse;
    }

    public List<MessageResponse> getMessages(Long conversationId, int page, int size) {
        Account currentAccount = getCurrentAccount();

        // Verify membership
        if (!conversationMemberRepository.existsByConversationConversationIdAndAccountAccountIdAndIsActiveTrue(
                conversationId, currentAccount.getAccountId())) {
            throw new RuntimeException("You are not a member of this conversation");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository
                .findByConversationConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        conversationId, pageable);

        return messages.getContent().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    private MessageResponse mapToMessageResponse(Message message) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .sender(mapToUserInfo(message.getSender()))
                .content(message.getContent())
                .contentType(message.getContentType())
                .isDeleted(message.getIsDeleted())
                .isPinned(message.getIsPinned())
                .parentId(message.getParent() != null ? message.getParent().getMessageId() : null)
                .replyCount(message.getReplies().size())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
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
