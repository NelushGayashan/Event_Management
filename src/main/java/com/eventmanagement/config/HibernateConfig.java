// src/main/java/com/eventmanagement/config/HibernateConfig.java
package com.eventmanagement.config;

import com.eventmanagement.service.SoftDeleteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class HibernateConfig implements WebMvcConfigurer {

    @Autowired
    private SoftDeleteService softDeleteService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                softDeleteService.enableSoftDeleteFilter();
                return true;
            }
        });
    }
}