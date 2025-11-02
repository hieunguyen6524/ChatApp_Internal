package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.SendMessageRequest;
import com.example.ChatApp_Internal.dto.response.MessageResponse;
import com.example.ChatApp_Internal.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversations/{conversationId}")
    public MessageResponse sendMessage(
            @DestinationVariable Long conversationId,
            @Payload SendMessageRequest request,
            Principal principal
    ) {

        System.out.println(principal.getName());

        log.info("WebSocket message received from {} to conversation {}",
                principal.getName(), conversationId);
        return messageService.sendMessage(request, principal);
    }
}
