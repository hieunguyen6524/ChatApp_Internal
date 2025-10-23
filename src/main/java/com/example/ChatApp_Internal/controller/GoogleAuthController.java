package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.GoogleAuthRequest;
import com.example.ChatApp_Internal.dto.request.GoogleIdTokenRequest;
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

    /**
     * Get Google OAuth2 authorization URL
     * Frontend redirects user to this URL
     */
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

    /**
     * Backend callback - Google redirects here after user authorization
     * This endpoint redirects to frontend with code
     */
    @GetMapping("/callback")
    public void handleGoogleCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {

        log.info("Received Google OAuth2 callback with code");

        // Redirect to frontend with code
        String redirectUrl = frontendOAuthRedirect +
                "?code=" + code +
                (state != null ? "&state=" + state : "");

        response.sendRedirect(redirectUrl);
    }

    /**
     * Exchange authorization code for tokens and authenticate user
     * Frontend calls this endpoint with the code
     */
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateWithGoogle(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse httpResponse) {

        log.info("Processing Google authentication with code");

        AuthResponse authResponse = googleAuthService.authenticateWithGoogle(request);

        // Set refresh token in cookie if available
        // Note: The refresh token should be extracted from authResponse
        // and set in cookie here or in the service
        String refreshToken = authResponse.getRefreshToken();
        cookieUtil.addRefreshTokenCookie(httpResponse, refreshToken);

        return ResponseEntity.ok(ApiResponse.success(
                "Google authentication successful",
                authResponse
        ));
    }

    /**
     * Authenticate with Google ID token (alternative flow)
     * For Google Sign-In Button in frontend
     */
    @PostMapping("/id-token")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateWithIdToken(
            @Valid @RequestBody GoogleIdTokenRequest request,
            HttpServletResponse httpResponse) {

        log.info("Processing Google authentication with ID token");

        AuthResponse authResponse = googleAuthService.authenticateWithIdToken(request);

        return ResponseEntity.ok(ApiResponse.success(
                "Google authentication successful",
                authResponse
        ));
    }

    /**
     * Link existing account with Google
     * Requires user to be authenticated
     */
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<Void>> linkGoogleAccount(
            @Valid @RequestBody GoogleIdTokenRequest request) {

        log.info("Linking Google account to existing user");

        // TODO: Implement account linking
        // Verify user is authenticated
        // Verify Google account
        // Link to existing account

        return ResponseEntity.ok(ApiResponse.success(
                "Google account linked successfully",
                null
        ));
    }

    /**
     * Unlink Google account
     * Requires user to be authenticated
     */
    @PostMapping("/unlink")
    public ResponseEntity<ApiResponse<Void>> unlinkGoogleAccount() {
        log.info("Unlinking Google account");

        // TODO: Implement account unlinking
        // Verify user is authenticated
        // Verify user has password set (cannot unlink if no password)
        // Update provider to LOCAL

        return ResponseEntity.ok(ApiResponse.success(
                "Google account unlinked successfully",
                null
        ));
    }
}