package com.example.ChatApp_Internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactToMessageRequest {
    @NotBlank(message = "Emoji is required")
    private String emoji;
}