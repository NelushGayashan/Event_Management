// src/main/java/com/eventmanagement/repository/AttendanceRepository.java
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
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {

    @Query("SELECT a FROM Attendance a WHERE a.event.id = :eventId AND a.deletedAt IS NULL")
    List<Attendance> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.deletedAt IS NULL")
    Page<Attendance> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.event.id = :eventId AND a.status = :status AND a.deletedAt IS NULL")
    List<Attendance> findByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") AttendanceStatus status);

    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.event.id = :eventId AND a.deletedAt IS NULL GROUP BY a.status")
    List<Object[]> countAttendanceByStatus(@Param("eventId") UUID eventId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a WHERE a.event.id = :eventId AND a.user.id = :userId AND a.deletedAt IS NULL")
    boolean existsByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);

    @Query("SELECT a FROM Attendance a WHERE a.event.id = :eventId AND a.user.id = :userId AND a.deletedAt IS NULL")
    Optional<Attendance> findByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.event.id = :eventId AND a.status = :status AND a.deletedAt IS NULL")
    Long countByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") AttendanceStatus status);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId AND a.status = 'GOING' AND a.deletedAt IS NULL " +
            "AND a.event.startTime > CURRENT_TIMESTAMP ORDER BY a.event.startTime ASC")
    List<Attendance> findUpcomingAttendanceByUserId(@Param("userId") UUID userId);

    @Query("SELECT a FROM Attendance a WHERE a.event.id = :eventId")
    List<Attendance> findByEventIdIncludingDeleted(@Param("eventId") UUID eventId);
}
