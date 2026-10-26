package com.healthcare.childrecords.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * CSRF defence for cookie-authenticated requests: a state-changing request that carries the auth
 * cookie (and no Authorization header) must come from one of the allowed origins.
 */
public class CookieOriginFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final AuthCookieService cookieService;
    private final List<String> allowedOrigins;

    public CookieOriginFilter(AuthCookieService cookieService, List<String> allowedOrigins) {
        this.cookieService = cookieService;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean unsafe = !SAFE_METHODS.contains(request.getMethod());
        boolean cookieAuth = request.getHeader("Authorization") == null
                && cookieService.read(request).isPresent();

        if (unsafe && cookieAuth && !allowedOrigins.contains(request.getHeader("Origin"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin not allowed");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
