package com.example.ChatApp_Internal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private Long fileId;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String fileType;
    private String url;
    private Long createdAt;
}

