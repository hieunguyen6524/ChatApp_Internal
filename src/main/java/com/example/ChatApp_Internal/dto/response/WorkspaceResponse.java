package com.example.ChatApp_Internal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private Long workspaceId;
    private String name;
    private String description;
    private UserInfo createdBy;
    private Boolean isArchived;
    private Long memberCount;
    private String userRole;
    private Long createdAt;
    private Long updatedAt;
}
