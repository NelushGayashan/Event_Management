// src/main/java/com/eventmanagement/repository/EventRepository.java
package com.eventmanagement.repository;

import com.eventmanagement.dto.response.EventWithAttendeeCountResponse;
import com.eventmanagement.entity.Event;
import com.eventmanagement.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL")
    List<Event> findAllActive();

    @Query("SELECT e FROM Event e WHERE e.startTime > :now AND e.deletedAt IS NULL")
    List<Event> findUpcomingActiveEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e")
    List<Event> findAllIncludingDeleted();

    @Query("SELECT e FROM Event e WHERE e.host.id = :hostId AND e.deletedAt IS NULL")
    Page<Event> findByHostId(@Param("hostId") UUID hostId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.visibility = :visibility AND e.deletedAt IS NULL")
    Page<Event> findByVisibility(@Param("visibility") Visibility visibility, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startTime > :now AND e.visibility = 'PUBLIC' AND e.deletedAt IS NULL ORDER BY e.startTime ASC")
    Page<Event> findUpcomingPublicEvents(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startTime >= :startDate AND e.endTime <= :endDate AND e.deletedAt IS NULL")
    Page<Event> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);

    @Query("SELECT e FROM Event e WHERE LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%')) AND e.deletedAt IS NULL")
    Page<Event> findByLocationContainingIgnoreCase(@Param("location") String location, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN e.attendances a " +
            "WHERE a.user.id = :userId AND a.deletedAt IS NULL AND e.deletedAt IS NULL")
    Page<Event> findEventsByAttendeeId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
            "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:startDate IS NULL OR e.startTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.endTime <= :endDate) AND " +
            "(:visibility IS NULL OR e.visibility = :visibility) AND " +
            "(:hostId IS NULL OR e.host.id = :hostId) AND " +
            "e.deletedAt IS NULL")
    Page<Event> findEventsWithFilters(@Param("title") String title,
                                      @Param("location") String location,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("visibility") Visibility visibility,
                                      @Param("hostId") UUID hostId,
                                      Pageable pageable);

    @Query("SELECT new com.eventmanagement.dto.response.EventWithAttendeeCountResponse(e, COUNT(a.id)) " +
            "FROM Event e LEFT JOIN e.attendances a " +
            "WHERE e.id = :eventId AND e.deletedAt IS NULL GROUP BY e")
    Optional<EventWithAttendeeCountResponse> findEventWithAttendeeCount(@Param("eventId") UUID eventId);

    @Query("SELECT e FROM Event e WHERE e.endTime BETWEEN :now AND :soon AND e.deletedAt IS NULL")
    List<Event> findEventsEndingSoon(@Param("now") LocalDateTime now,
                                     @Param("soon") LocalDateTime soon);
}
