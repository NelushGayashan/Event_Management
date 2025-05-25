// src/main/java/com/eventmanagement/config/MethodSecurityConfig.java
package com.eventmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig { }
