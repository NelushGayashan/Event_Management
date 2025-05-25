// src/test/java/com/eventmanagement/config/TestJpaConfig.java
package com.eventmanagement.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@TestConfiguration
public class TestJpaConfig {

    @Bean
    @Primary
    public TestEntityManager testEntityManager(EntityManager entityManager) {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("softDeleteFilter").setParameter("isDeleted", false);

        return new TestEntityManager((EntityManagerFactory) entityManager);
    }
}
