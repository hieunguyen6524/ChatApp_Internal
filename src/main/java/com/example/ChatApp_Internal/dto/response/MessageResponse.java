package com.example.ChatApp_Internal.dto.response;

import com.example.ChatApp_Internal.entity.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long messageId;
    private Long conversationId;
    private UserInfo sender;
    private String content;
    private ContentType contentType;
    private Boolean isDeleted;
    private Boolean isPinned;
    private Long parentId;
    private Integer replyCount;
    private List<ReactionResponse> reactions;
    private List<UserInfo> mentions;
    private List<FileResponse> attachments;
    private Long createdAt;
    private Long updatedAt;
}

