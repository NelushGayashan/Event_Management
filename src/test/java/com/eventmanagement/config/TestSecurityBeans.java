// src/test/java/com/eventmanagement/config/TestSecurityBeans.java
package com.eventmanagement.config;

import com.eventmanagement.security.JwtAuthenticationEntryPoint;
import com.eventmanagement.security.JwtAuthenticationFilter;
import com.eventmanagement.security.JwtTokenProvider;
import com.eventmanagement.service.AuthService;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.FilterService;
import com.eventmanagement.service.SoftDeleteService;
import com.eventmanagement.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityBeans {

    @Bean
    @Primary
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return mock(JwtAuthenticationEntryPoint.class);
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return mock(JwtAuthenticationFilter.class);
    }

    @Bean
    @Primary
    public JwtTokenProvider jwtTokenProvider() {
        return mock(JwtTokenProvider.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return mock(UserDetailsService.class);
    }

    @Bean
    @Primary
    public AuthService authService() {
        return mock(AuthService.class);
    }

    @Bean
    @Primary
    public UserService userService() {
        return mock(UserService.class);
    }

    @Bean
    @Primary
    public EventService eventService() {
        return mock(EventService.class);
    }

    @Bean
    @Primary
    public FilterService filterService() {
        return mock(FilterService.class);
    }

    @Bean
    @Primary
    public SoftDeleteService softDeleteService() {
        return mock(SoftDeleteService.class);
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean("tokenBlacklistCacheManager")
    public CacheManager tokenBlacklistCacheManager() {
        return new ConcurrentMapCacheManager("tokenBlacklist");
    }
}
