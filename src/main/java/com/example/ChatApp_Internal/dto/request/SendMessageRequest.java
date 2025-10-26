package com.example.ChatApp_Internal.dto.request;

import com.example.ChatApp_Internal.entity.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotBlank(message = "Message content is required")
    private String content;

    private ContentType contentType = ContentType.TEXT;

    private Long parentId; // For threads

    private List<Long> mentionedUserIds; // For @mentions

    private List<Long> attachmentIds; // File attachments
}
