// src/test/java/com/eventmanagement/config/TestMethodSecurityConfig.java
package com.eventmanagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@TestConfiguration
@EnableMethodSecurity(prePostEnabled = true)
public class TestMethodSecurityConfig {}
