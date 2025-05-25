// src/main/java/com/eventmanagement/service/AttendanceService.java
package com.eventmanagement.service;

import com.eventmanagement.entity.Attendance;
import com.eventmanagement.repository.AttendanceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private FilterService filterService;

    @PersistenceContext
    private EntityManager entityManager;

    public void softDeleteAttendance(UUID eventId, UUID userId) {
        filterService.enableSoftDeleteFilter();

        Optional<Attendance> attendance = attendanceRepository
                .findByEventIdAndUserId(eventId, userId);

        if (attendance.isPresent()) {
            attendance.get().softDelete();
            attendanceRepository.save(attendance.get());
        }
    }

    @Transactional(readOnly = true)
    public List<Attendance> findAllIncludingDeleted(UUID eventId) {
        return entityManager.createQuery(
                        "SELECT a FROM Attendance a WHERE a.event.id = :eventId",
                        Attendance.class)
                .setParameter("eventId", eventId)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public List<Attendance> findActiveAttendances(UUID eventId) {
        filterService.enableSoftDeleteFilter();
        return attendanceRepository.findByEventId(eventId);
    }

    public void restoreAttendance(UUID eventId, UUID userId) {
        filterService.withoutSoftDeleteFilter(() -> {
            Optional<Attendance> attendance = attendanceRepository
                    .findByEventIdAndUserId(eventId, userId);

            if (attendance.isPresent() && attendance.get().isDeleted()) {
                attendance.get().setDeletedAt(null);
                attendanceRepository.save(attendance.get());
            }
        });
    }
}
