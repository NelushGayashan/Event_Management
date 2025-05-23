package com.eventmanagement.repository;

import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.AttendanceId;
import com.eventmanagement.enums.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {

    // Find attendance by event
    List<Attendance> findByEventId(UUID eventId);

    // Find attendance by user
    Page<Attendance> findByUserId(UUID userId, Pageable pageable);

    // Find attendance by event and status
    List<Attendance> findByEventIdAndStatus(UUID eventId, AttendanceStatus status);

    // Count attendees by status for an event
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.event.id = :eventId GROUP BY a.status")
    List<Object[]> countAttendanceByStatus(@Param("eventId") UUID eventId);

    // Check if user is attending event
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    // Get user's attendance for event
    Optional<Attendance> findByEventIdAndUserId(UUID eventId, UUID userId);

    // Get attendance statistics
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event.id = :eventId AND a.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") AttendanceStatus status);

    // Find events user is going to attend
    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.status = 'GOING' " +
            "AND a.event.startTime > CURRENT_TIMESTAMP ORDER BY a.event.startTime ASC")
    List<Attendance> findUpcomingAttendanceByUserId(@Param("userId") UUID userId);
}