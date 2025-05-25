// src/main/java/com/eventmanagement/service/FilterService.java
package com.eventmanagement.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FilterService {

    @Autowired
    private EntityManager entityManager;

    public void enableSoftDeleteFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("softDeleteFilter").setParameter("isDeleted", false);
    }

    public void disableSoftDeleteFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("softDeleteFilter");
    }

    public void withoutSoftDeleteFilter(Runnable operation) {
        disableSoftDeleteFilter();
        try {
            operation.run();
        } finally {
            enableSoftDeleteFilter();
        }
    }
}
