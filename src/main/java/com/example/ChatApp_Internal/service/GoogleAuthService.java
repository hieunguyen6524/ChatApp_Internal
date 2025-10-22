package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.dto.google.GoogleTokenResponse;
import com.example.ChatApp_Internal.dto.google.GoogleUserInfo;
import com.example.ChatApp_Internal.dto.request.GoogleAuthRequest;
import com.example.ChatApp_Internal.dto.request.GoogleIdTokenRequest;
import com.example.ChatApp_Internal.dto.response.AuthResponse;
import com.example.ChatApp_Internal.dto.response.UserInfo;
import com.example.ChatApp_Internal.entity.*;
import com.example.ChatApp_Internal.repository.AccountRepository;
import com.example.ChatApp_Internal.repository.ProfileRepository;
import com.example.ChatApp_Internal.repository.RefreshContextRepository;
import com.example.ChatApp_Internal.repository.RoleRepository;
import com.example.ChatApp_Internal.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final RefreshContextRepository refreshContextRepository;
    private final JwtService jwtService;
    private final GoogleOAuth2Service googleOAuth2Service;

    @Value("${app.frontend-oauth-redirect}")
    private String frontendOAuthRedirect;

    /**
     * Authenticate with Google using authorization code
     */
    @Transactional
    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) {
        // Exchange code for token
        System.out.println("redirec" + request.getRedirectUri());
        GoogleTokenResponse tokenResponse = googleOAuth2Service.exchangeCodeForToken(
                request.getCode(),
                request.getRedirectUri()
        );

        // Get user info
        GoogleUserInfo googleUser = googleOAuth2Service.getUserInfo(tokenResponse.getAccessToken());

        // Process user (create or login)
        return processGoogleUser(googleUser);
    }

    /**
     * Authenticate with Google using ID token (for frontend direct flow)
     */
    @Transactional
    public AuthResponse authenticateWithIdToken(GoogleIdTokenRequest request) {
        // Verify and decode ID token
        GoogleUserInfo googleUser = googleOAuth2Service.verifyIdToken(request.getIdToken());

        // Process user (create or login)
        return processGoogleUser(googleUser);
    }

    /**
     * Process Google user - create new account or login existing
     */
    private AuthResponse processGoogleUser(GoogleUserInfo googleUser) {
        Account account = accountRepository.findByEmail(googleUser.getEmail())
                .orElse(null);

        if (account == null) {
            // Create new account
            account = createGoogleAccount(googleUser);
            log.info("New Google account created: {}", googleUser.getEmail());
        } else {
            // Verify provider
            if (account.getProvider() != AuthProvider.GOOGLE) {
                throw new RuntimeException(
                        "Email already registered with " + account.getProvider() + " provider. " +
                                "Please login using your email and password."
                );
            }

            // Update profile if needed
            updateGoogleProfile(account, googleUser);
            log.info("Existing Google account logged in: {}", googleUser.getEmail());
        }

        // Generate tokens
        List<String> roles = account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        String contextId = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(account.getEmail(), roles);
        String refreshToken = jwtService.generateRefreshToken(account.getEmail(), contextId);

        // Save refresh context
        RefreshContext refreshContext = RefreshContext.builder()
                .account(account)
                .contextId(contextId)
                .expiryDate(System.currentTimeMillis() + jwtService.getRefreshTokenExpirationMs())
                .revoked(false)
                .build();
        refreshContextRepository.save(refreshContext);

        // Update profile status
        if (account.getProfile() != null) {
            account.getProfile().setStatus(UserStatus.ONLINE);
            account.getProfile().setLastActiveAt(System.currentTimeMillis());
            profileRepository.save(account.getProfile());
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userInfo(mapToUserInfo(account, account.getProfile()))
                .build();
    }

    /**
     * Create new account from Google user info
     */
    private Account createGoogleAccount(GoogleUserInfo googleUser) {
        Optional<Account> existing = accountRepository.findByEmail(googleUser.getEmail());
        if (existing.isPresent()) {
            return existing.get(); // Nếu user đã có, trả về luôn
        }
        // Create account
        Account account = Account.builder()
                .email(googleUser.getEmail())
                .password(null) // No password for OAuth users
                .provider(AuthProvider.GOOGLE)
                .isVerified(googleUser.getEmailVerified() != null ? googleUser.getEmailVerified() : true)
                .isActive(true)
                .build();

        // Assign MEMBER role
        Role memberRole = roleRepository.findByRoleName("MEMBER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        account.setRoles(Set.of(memberRole));

        account = accountRepository.save(account);

        // Create profile
        String username = generateUniqueUsername(googleUser.getEmail(), googleUser.getGivenName());

        Profile profile = Profile.builder()
                .account(account)
                .username(username)
                .displayName(googleUser.getName() != null ? googleUser.getName() : googleUser.getEmail())
                .avatarUrl(googleUser.getPicture())
                .status(UserStatus.OFFLINE)
                .build();
        profileRepository.save(profile);

        return account;
    }

    /**
     * Update existing Google account profile
     */
    private void updateGoogleProfile(Account account, GoogleUserInfo googleUser) {
        Profile profile = account.getProfile();

        if (profile != null) {
            boolean updated = false;

            // Update avatar if changed
            if (googleUser.getPicture() != null &&
                    !googleUser.getPicture().equals(profile.getAvatarUrl())) {
                profile.setAvatarUrl(googleUser.getPicture());
                updated = true;
            }

            // Update display name if changed
            if (googleUser.getName() != null &&
                    !googleUser.getName().equals(profile.getDisplayName())) {
                profile.setDisplayName(googleUser.getName());
                updated = true;
            }

            if (updated) {
                profileRepository.save(profile);
                log.info("Updated profile for Google user: {}", googleUser.getEmail());
            }
        }
    }

    /**
     * Generate unique username from email and name
     */
    private String generateUniqueUsername(String email, String givenName) {
        String baseUsername;

        if (givenName != null && !givenName.isEmpty()) {
            baseUsername = givenName.toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }

        // Ensure minimum length
        if (baseUsername.length() < 3) {
            baseUsername = "user" + baseUsername;
        }

        // Check if username exists
        String username = baseUsername;
        int counter = 1;

        while (profileRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * Map Account and Profile to UserInfo
     */
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

