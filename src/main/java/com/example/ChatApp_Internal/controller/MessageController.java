package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.SendMessageRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.MessageResponse;
import com.example.ChatApp_Internal.dto.response.PageResponse;
import com.example.ChatApp_Internal.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
@PreAuthorize("isAuthenticated()")
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageResponse message = messageService.sendMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", message));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MessageResponse> messages = messageService.getMessages(conversationId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}
