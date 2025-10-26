package com.example.ChatApp_Internal.dto.response;

import com.example.ChatApp_Internal.entity.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long conversationId;
    private Long workspaceId;
    private String name;
    private String description;
    private ConversationType type;
    private Boolean isPrivate;
    private Boolean isArchived;
    private UserInfo createdBy;
    private Long memberCount;
    private Long unreadCount;
    private MessageResponse lastMessage;
    private Long createdAt;
    private Long updatedAt;
}