// src/main/java/com/eventmanagement/service/SoftDeleteService.java
package com.eventmanagement.service;

import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SoftDeleteService {

    @PersistenceContext
    private EntityManager entityManager;

    public void enableSoftDeleteFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("softDeleteFilter");
    }

    public void disableSoftDeleteFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("softDeleteFilter");
    }

    public <T> T softDelete(T entity) {
        if (entity instanceof User) {
            ((User) entity).setDeletedAt(LocalDateTime.now());
        } else if (entity instanceof Event) {
            ((Event) entity).setDeletedAt(LocalDateTime.now());
        } else if (entity instanceof Attendance) {
            ((Attendance) entity).setDeletedAt(LocalDateTime.now());
        }
        return entityManager.merge(entity);
    }
}