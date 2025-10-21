package com.example.ChatApp_Internal.dto.response;


import com.example.ChatApp_Internal.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private Long accountId;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private UserStatus status;
    private Set<String> roles;
    private Boolean isVerified;
}