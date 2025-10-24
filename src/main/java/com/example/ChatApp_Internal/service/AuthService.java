package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.request.ForgotPasswordRequest;
import com.example.ChatApp_Internal.dto.request.LoginRequest;
import com.example.ChatApp_Internal.dto.request.RegisterRequest;
import com.example.ChatApp_Internal.dto.request.ResetPasswordRequest;
import com.example.ChatApp_Internal.dto.response.AuthResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.entity.*;
import com.example.ChatApp_Internal.repository.*;
import com.example.ChatApp_Internal.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final RefreshContextRepository refreshContextRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    @Value("${app.mail.forgot_password_token-ms}")
    private Long forgotPasswordTokenMs;
    @Value("${app.mail.verify_email_token-ms}")
    private Long verifyEmailTokenMs;


    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Check if username already exists
        if (profileRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        // Create account
        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .isVerified(false)
                .isActive(true)
                .build();

        // Assign MEMBER role by default
        Role memberRole = roleRepository.findByRoleName("MEMBER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        account.setRoles(Set.of(memberRole));

        account = accountRepository.save(account);

        // Create profile
        Profile profile = Profile.builder()
                .account(account)
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .status(UserStatus.OFFLINE)
                .build();
        profileRepository.save(profile);

        // Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .account(account)
                .token(token)
                .type(TokenType.EMAIL_VERIFICATION)
                .expiryDate(System.currentTimeMillis() + verifyEmailTokenMs)
                .used(false)
                .build();
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(account.getEmail(), token);

        log.info("User registered successfully: {}", request.getEmail());

        // Return auth response without logging in
        return AuthResponse.builder()
                .accessToken(null)
                .userInfo(mapToUserInfo(account, profile))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!account.getIsVerified()) {
            throw new RuntimeException("Email not verified. Please verify your email");
        }

        // Generate tokens
        List<String> roles = account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        String contextId = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(account.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(account.getEmail(), contextId);

        refreshContextRepository.revokeAllActiveByAccountId(account.getAccountId());
        log.info("Revoked all active refresh tokens for user {}", account.getAccountId());

        // Save refresh context
        RefreshContext refreshContext = RefreshContext.builder()
                .account(account)
                .contextId(contextId)
                .expiryDate(System.currentTimeMillis() + jwtService.getRefreshTokenExpirationMs())
                .revoked(false)
                .build();
        refreshContextRepository.save(refreshContext);

        // Set refresh token in cookie (handled by controller)

        // Update profile status
        if (account.getProfile() != null) {
            account.getProfile().setStatus(UserStatus.ONLINE);
            account.getProfile().setLastActiveAt(System.currentTimeMillis());
            profileRepository.save(account.getProfile());
        }


        log.info("User logged in successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userInfo(mapToUserInfo(account, account.getProfile()))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String contextId = jwtService.getContextIdFromToken(refreshToken);
        RefreshContext refreshContext = refreshContextRepository.findByContextId(contextId)
                .orElseThrow(() -> new RuntimeException("Refresh context not found"));

        if (refreshContext.getRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshContext.getExpiryDate() < System.currentTimeMillis()) {
            throw new RuntimeException("Refresh token expired");
        }

        Account account = refreshContext.getAccount();
        List<String> roles = account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        if (!account.getIsActive()) {
            throw new RuntimeException("Account is not active");
        }

        String newContextId = UUID.randomUUID().toString();
        String newAccessToken = jwtService.generateAccessToken(account.getEmail(), roles);
        String newRefreshToken = jwtService.generateRefreshToken(account.getEmail(), newContextId);

        // Revoke old refresh context
        refreshContext.setRevoked(true);
        refreshContextRepository.save(refreshContext);


        RefreshContext newRefreshContext = RefreshContext.builder()
                .account(account)
                .contextId(newContextId)
                .expiryDate(System.currentTimeMillis() + jwtService.getRefreshTokenExpirationMs())
                .revoked(false)
                .build();
        refreshContextRepository.save(newRefreshContext);

        log.info("Token refreshed successfully for user: {}", account.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .userInfo(mapToUserInfo(account, account.getProfile()))
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Blacklist access token
        if (accessToken != null && jwtService.validateToken(accessToken)) {
            String email = jwtService.getEmailFromToken(accessToken);
            Account account = accountRepository.findByEmail(email).orElse(null);

            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(accessToken)
                    .account(account)
                    .reason("LOGOUT")
                    .expiresAt(jwtService.getExpirationDate(accessToken).getTime())
                    .build();
            blacklistedTokenRepository.save(blacklistedToken);

            // Update profile status
            if (account != null && account.getProfile() != null) {
                account.getProfile().setStatus(UserStatus.OFFLINE);
                account.getProfile().setLastActiveAt(System.currentTimeMillis());
                profileRepository.save(account.getProfile());
            }
        }

        // Revoke refresh context
        if (refreshToken != null && jwtService.validateToken(refreshToken)) {
            String contextId = jwtService.getContextIdFromToken(refreshToken);
            refreshContextRepository.findByContextId(contextId).ifPresent(context -> {
                context.setRevoked(true);
                refreshContextRepository.save(context);
            });

        }

        log.info("User logged out successfully");
    }

    @Transactional
    public void verifyEmail(String token) {
        System.out.println(verifyEmailTokenMs);
        VerificationToken verificationToken = verificationTokenRepository
                .findByTokenAndType(token, TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.getUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (verificationToken.getExpiryDate() < System.currentTimeMillis()) {
            throw new RuntimeException("Token expired");
        }

        Account account = verificationToken.getAccount();
        account.setIsVerified(true);
        accountRepository.save(account);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified successfully for: {}", account.getEmail());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = VerificationToken.builder()
                .account(account)
                .token(token)
                .type(TokenType.RESET_PASSWORD)
                .expiryDate(System.currentTimeMillis() + forgotPasswordTokenMs) // 1 hour
                .used(false)
                .build();
        verificationTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(account.getEmail(), token);

        log.info("Password reset email sent to: {}", request.getEmail());
    }

    @Transactional
    public void resetPassword(String token, ResetPasswordRequest request) {
        VerificationToken resetToken = verificationTokenRepository
                .findByTokenAndType(token, TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.getUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (resetToken.getExpiryDate() < System.currentTimeMillis()) {
            throw new RuntimeException("Token expired");
        }

        Account account = resetToken.getAccount();
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        resetToken.setUsed(true);
        verificationTokenRepository.save(resetToken);

        // Revoke all existing refresh tokens
        refreshContextRepository.revokeAllByAccountId(account.getAccountId());

        log.info("Password reset successfully for: {}", account.getEmail());
    }

    private UserInfo mapToUserInfo(Account account, Profile profile) {
        Set<String> roleNames = account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        return UserInfo.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .username(profile != null ? profile.getUsername() : null)
                .displayName(profile != null ? profile.getDisplayName() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .bio(profile != null ? profile.getBio() : null)
                .status(profile != null ? profile.getStatus() : UserStatus.OFFLINE)
                .roles(roleNames)
                .isVerified(account.getIsVerified())
                .build();
    }
}
