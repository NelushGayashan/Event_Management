// src/test/java/com/eventmanagement/repository/BaseRepositoryTest.java
package com.eventmanagement.repository;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public abstract class BaseRepositoryTest {

    @Autowired
    protected TestEntityManager entityManager;

    @BeforeEach
    void setUpBaseTest() {
        Session session = entityManager.getEntityManager().unwrap(Session.class);
        session.enableFilter("softDeleteFilter").setParameter("isDeleted", false);
    }
}
