package com.eventmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final CacheManager tokenBlacklistCacheManager;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserDetailsService userDetailsService,
                                   @Qualifier("tokenBlacklistCacheManager") CacheManager tokenBlacklistCacheManager) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistCacheManager = tokenBlacklistCacheManager;
    }

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistCacheManager = null;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // ✅ Check token blacklist (if cache is available)
                if (isTokenBlacklisted(jwt)) {
                    handleBlacklistedToken(response);
                    return;
                }

                if (tokenProvider.validateToken(jwt)) {
                    authenticateUser(jwt, request);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenBlacklisted(String jwt) {
        if (tokenBlacklistCacheManager == null) {
            return false;
        }

        try {
            Cache tokenBlacklistCache = tokenBlacklistCacheManager.getCache("tokenBlacklist");
            if (tokenBlacklistCache != null) {
                Boolean isBlacklisted = tokenBlacklistCache.get(jwt, Boolean.class);
                return Boolean.TRUE.equals(isBlacklisted);
            }
        } catch (Exception ex) {
            logger.warn("Error checking token blacklist: {}", ex.getMessage());
        }
        return false;
    }

    private void handleBlacklistedToken(HttpServletResponse response) throws IOException {
        logger.warn("Blocked blacklisted JWT token");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Token has been invalidated\",\"message\":\"Please login again\"}");
    }

    private void authenticateUser(String jwt, HttpServletRequest request) {
        String userId = tokenProvider.getUserIdFromToken(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}