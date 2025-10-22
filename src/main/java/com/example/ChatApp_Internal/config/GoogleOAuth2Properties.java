package com.example.ChatApp_Internal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.oauth2")
@Data
public class GoogleOAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
