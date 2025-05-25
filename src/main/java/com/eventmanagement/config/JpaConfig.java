// src/main/java/com/eventmanagement/config/JpaConfig.java
package com.eventmanagement.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@ConditionalOnProperty(name = "spring.jpa.enabled", havingValue = "true", matchIfMissing = true)
public class JpaConfig {
}
