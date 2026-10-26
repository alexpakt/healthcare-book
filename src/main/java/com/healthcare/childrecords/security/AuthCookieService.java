package com.healthcare.childrecords.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class AuthCookieService {

    private final String cookieName;
    private final boolean secure;
    private final String sameSite;
    private final Duration maxAge;

    public AuthCookieService(@Value("${app.auth.cookie.name:access_token}") String cookieName,
                             @Value("${app.auth.cookie.secure:true}") boolean secure,
                             @Value("${app.auth.cookie.same-site:Lax}") String sameSite,
                             @Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAge = Duration.ofMillis(jwtExpirationMs);
    }

    public String create(String token) {
        return build(token, maxAge).toString();
    }

    public String clear() {
        return build("", Duration.ZERO).toString();
    }

    public Optional<String> read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private ResponseCookie build(String value, Duration age) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(age)
                .build();
    }
}
