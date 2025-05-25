// src/main/java/com/eventmanagement/config/SoftDeleteFilterConfig.java
package com.eventmanagement.config;

import com.eventmanagement.service.SoftDeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class SoftDeleteFilterConfig {

    @Autowired
    private SoftDeleteService softDeleteService;

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        softDeleteService.enableSoftDeleteFilter();
    }

    @PostConstruct
    public void init() {
        softDeleteService.enableSoftDeleteFilter();
    }
}
