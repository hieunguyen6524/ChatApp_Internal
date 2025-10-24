package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.GoogleAuthRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.AuthResponse;
import com.example.ChatApp_Internal.service.GoogleAuthService;
import com.example.ChatApp_Internal.service.GoogleOAuth2Service;
import com.example.ChatApp_Internal.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend-oauth-redirect}")
    private String frontendOAuthRedirect;


    @GetMapping("/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        String authUrl = googleOAuth2Service.getAuthorizationUrl(state);

        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        response.put("state", state);

        log.info("Generated Google OAuth2 authorization URL");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/callback")
    public void handleGoogleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {

        log.info("Received Google OAuth2 callback with code");
        
        String redirectUrl = frontendOAuthRedirect +
                "?code=" + code +
                (state != null ? "&state=" + state : "");

        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse httpResponse) {

        log.info("Processing Google authentication with code");

        AuthResponse authResponse = googleAuthService.authenticateWithGoogle(request);

        String refreshToken = authResponse.getRefreshToken();
        cookieUtil.addRefreshTokenCookie(httpResponse, refreshToken);

        return ResponseEntity.ok(ApiResponse.success(
                "Google authentication successful",
                authResponse
        ));
    }
}