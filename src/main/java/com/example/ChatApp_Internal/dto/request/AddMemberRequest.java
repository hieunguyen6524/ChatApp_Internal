package com.example.ChatApp_Internal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {
    @NotNull(message = "Account ID is required")
    private Long accountId;

    private String roleName = "MEMBER";
}