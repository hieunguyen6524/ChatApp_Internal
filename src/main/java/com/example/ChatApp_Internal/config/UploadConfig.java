package com.example.ChatApp_Internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "upload")
public class UploadConfig {
    private Long maxFileSize;
    private Long maxRequestSize;
    private String allowedExtensions;
    private String allowedImageExtensions;

    public List<String> getAllowedExtensionsList() {
        return Arrays.asList(allowedExtensions.split(","));
    }

    public List<String> getAllowedImageExtensionsList() {
        return Arrays.asList(allowedImageExtensions.split(","));
    }
}

