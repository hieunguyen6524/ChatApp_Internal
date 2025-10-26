package com.example.ChatApp_Internal.dto.request;

import com.example.ChatApp_Internal.entity.ConversationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConversationRequest {
    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;

    @NotBlank(message = "Conversation name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Conversation type is required")
    private ConversationType type;

    private Boolean isPrivate = false;

    private List<Long> memberIds; // For DM creation
}
