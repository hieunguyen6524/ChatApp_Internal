package com.example.ChatApp_Internal.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${app.cookie.refresh-token-name}")
    private String refreshCookieName;

    @Value("${app.cookie.refresh-token-max-age}")
    private int refreshCookieMaxAge;

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(refreshCookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production with HTTPS
        cookie.setPath("/api/auth/");
        cookie.setMaxAge(refreshCookieMaxAge);
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshCookieName, "");
        cookie.setPath("/api/auth/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public Optional<String> getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> refreshCookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
