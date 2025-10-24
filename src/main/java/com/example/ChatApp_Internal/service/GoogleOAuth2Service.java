package com.example.ChatApp_Internal.service;

import com.example.ChatApp_Internal.config.GoogleOAuth2Properties;
import com.example.ChatApp_Internal.dto.google.GoogleTokenResponse;
import com.example.ChatApp_Internal.dto.google.GoogleUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private final GoogleOAuth2Properties googleProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAuthorizationUrl(String state) {
        String scope = "openid email profile";

        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + googleProperties.getClientId() +
                "&redirect_uri=" + googleProperties.getRedirectUri() +
                "&response_type=code" +
                "&scope=" + scope +
                "&access_type=offline" +
                "&prompt=consent" +
                (state != null ? "&state=" + state : "");
    }

    /**
     * Exchange authorization code for access token
     */
    public GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType
                    (MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", googleProperties.getClientId());
            params.add("client_secret", googleProperties.getClientSecret());
            params.add("redirect_uri", redirectUri != null ? redirectUri : googleProperties.getRedirectUri());
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                    GOOGLE_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    GoogleTokenResponse.class
            );

            log.info("Successfully exchanged code for Google token");
            return response.getBody();

        } catch (Exception e) {
            log.error("Error exchanging code for token: ", e);
            throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage());
        }
    }

    /**
     * Get user info using access token
     */
    public GoogleUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    entity,
                    GoogleUserInfo.class
            );

            log.info("Successfully retrieved Google user info");
            return response.getBody();

        } catch (Exception e) {
            log.error("Error getting user info: ", e);
            throw new RuntimeException("Failed to get user info: " + e.getMessage());
        }
    }

    /**
     * Verify and decode Google ID Token
     */
    public GoogleUserInfo verifyIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(googleProperties.getClientId()))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);

            if (token != null) {
                GoogleIdToken.Payload payload = token.getPayload();

                GoogleUserInfo userInfo = GoogleUserInfo.builder()
                        .id(payload.getSubject())
                        .email(payload.getEmail())
                        .emailVerified(payload.getEmailVerified())
                        .name((String) payload.get("name"))
                        .givenName((String) payload.get("given_name"))
                        .familyName((String) payload.get("family_name"))
                        .picture((String) payload.get("picture"))
                        .locale((String) payload.get("locale"))
                        .build();

                log.info("Successfully verified Google ID token for email: {}", userInfo.getEmail());
                return userInfo;
            } else {
                throw new RuntimeException("Invalid ID token");
            }

        } catch (Exception e) {
            log.error("Error verifying ID token: ", e);
            throw new RuntimeException("Failed to verify ID token: " + e.getMessage());
        }
    }
}
