// src/test/java/com/eventmanagement/config/TestJpaAuditingConfig.java
package com.eventmanagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class TestJpaAuditingConfig {}
