package com.example.ChatApp_Internal.controller;


import com.example.ChatApp_Internal.dto.request.AddMemberRequest;
import com.example.ChatApp_Internal.dto.request.CreateConversationRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.ConversationResponse;
import com.example.ChatApp_Internal.dto.response.MemberResponse;
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
        ConversationResponse conversation = conversationService.createConversation(request);
        return ResponseEntity.ok(ApiResponse.success("Conversation created successfully", conversation));
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getWorkspaceConversations(
            @PathVariable Long workspaceId
    ) {
        List<ConversationResponse> conversations = conversationService.getWorkspaceConversations(workspaceId);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(@PathVariable Long conversationId) {
        ConversationResponse conversation = conversationService.getConversationById(conversationId);

        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    @PostMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<MemberResponse>> addMember(
            @PathVariable Long conversationId,
            @Valid @RequestBody AddMemberRequest request) {
        MemberResponse member = conversationService.addConversationMember(conversationId, request);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", member));
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers(
            @PathVariable Long conversationId) {
        List<MemberResponse> members = conversationService.getConversationMembers(conversationId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @PatchMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> updateConversation(
            @PathVariable Long conversationId, @RequestBody CreateConversationRequest request) {
        ConversationResponse conversationResponse = conversationService.updateConversation(conversationId, request);
        return ResponseEntity.ok(ApiResponse.success(conversationResponse));
    }
 
    @DeleteMapping("{conversationId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long conversationId,
            @PathVariable Long memberId) {
        conversationService.removeMember(conversationId, memberId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @DeleteMapping("/{conversationId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveWorkspace(@PathVariable Long conversationId) {
        conversationService.leaveConversation(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Left conversation successfully", null));
    }
}
