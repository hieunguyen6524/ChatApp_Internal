package com.example.ChatApp_Internal.controller;


import com.example.ChatApp_Internal.dto.request.CreateConversationRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.ConversationResponse;
import com.example.ChatApp_Internal.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        ConversationResponse conversationResponse = conversationService.createConversation(request);
        return ResponseEntity.ok(ApiResponse.success("Conversation created successfully", conversationResponse));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getWorkspaceConversations(
            @PathVariable Long workspaceId
    ) {
        List<ConversationResponse> conversations = conversationService.getWorkspaceConversations(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

}
