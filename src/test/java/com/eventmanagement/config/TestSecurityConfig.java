// src/test/java/com/eventmanagement/config/TestSecurityConfig.java
package com.eventmanagement.config;

import com.eventmanagement.security.JwtTokenProvider;
import com.eventmanagement.service.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

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
        return new TestFilterService();
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
