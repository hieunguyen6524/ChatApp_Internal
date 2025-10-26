package com.example.ChatApp_Internal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long memberId;
    private UserInfo user;
    private String role;
    private Boolean isActive;
    private Long joinedAt;
}
