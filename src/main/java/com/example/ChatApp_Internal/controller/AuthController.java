package com.example.ChatApp_Internal.controller;

import com.example.ChatApp_Internal.dto.request.ForgotPasswordRequest;
import com.example.ChatApp_Internal.dto.request.LoginRequest;
import com.example.ChatApp_Internal.dto.request.RegisterRequest;
import com.example.ChatApp_Internal.dto.request.ResetPasswordRequest;
import com.example.ChatApp_Internal.dto.response.ApiResponse;
import com.example.ChatApp_Internal.dto.response.AuthResponse;
import com.example.ChatApp_Internal.service.AuthService;
import com.example.ChatApp_Internal.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful. Please check your email to verify your account.",
                        response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse) {

        log.info("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request, httpResponse);

        String refreshToken = response.getRefreshToken();
        cookieUtil.addRefreshTokenCookie(httpResponse, refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        String refreshToken = cookieUtil.getRefreshTokenFromRequest(request)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        AuthResponse response = authService.refreshToken(refreshToken);


        String newRefreshToken = response.getRefreshToken();
        cookieUtil.addRefreshTokenCookie(httpResponse, newRefreshToken);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request,
            HttpServletResponse httpResponse) {

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        String refreshToken = cookieUtil.getRefreshTokenFromRequest(request).orElse(null);

        authService.logout(accessToken, refreshToken);
        cookieUtil.clearRefreshTokenCookie(httpResponse);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request with token: {}", token);
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "If the email exists, a password reset link has been sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestParam String token
            , @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Reset password request");
        authService.resetPassword(token, request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestParam String email) {

        log.info("Resend verification request for email: {}", email);
        // Implement resend verification logic in service
        return ResponseEntity.ok(ApiResponse.success(
                "Verification email sent if account exists", null));
    }
}