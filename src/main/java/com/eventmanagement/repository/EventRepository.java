package com.eventmanagement.repository;

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

    // Find events by host
    Page<Event> findByHostId(UUID hostId, Pageable pageable);

    // Find public events
    Page<Event> findByVisibility(Visibility visibility, Pageable pageable);

    // Find upcoming events
    @Query("SELECT e FROM Event e WHERE e.startTime > :now AND e.visibility = 'PUBLIC' ORDER BY e.startTime ASC")
    Page<Event> findUpcomingPublicEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Find events by date range
    @Query("SELECT e FROM Event e WHERE e.startTime >= :startDate AND e.endTime <= :endDate")
    Page<Event> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate,
                                Pageable pageable);

    // Find events by location
    @Query("SELECT e FROM Event e WHERE LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Event> findByLocationContainingIgnoreCase(@Param("location") String location, Pageable pageable);

    // Find events user is attending
    @Query("SELECT e FROM Event e JOIN e.attendances a WHERE a.user.id = :userId")
    Page<Event> findEventsByAttendeeId(@Param("userId") UUID userId, Pageable pageable);

    // Complex search query
    @Query("SELECT e FROM Event e WHERE " +
            "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:startDate IS NULL OR e.startTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.endTime <= :endDate) AND " +
            "(:visibility IS NULL OR e.visibility = :visibility) AND " +
            "(:hostId IS NULL OR e.host.id = :hostId)")
    Page<Event> findEventsWithFilters(@Param("title") String title,
                                      @Param("location") String location,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("visibility") Visibility visibility,
                                      @Param("hostId") UUID hostId,
                                      Pageable pageable);

    // Get events with attendee count
    @Query("SELECT e, COUNT(a) FROM Event e LEFT JOIN e.attendances a " +
            "WHERE e.id = :eventId GROUP BY e")
    Optional<Object[]> findEventWithAttendeeCount(@Param("eventId") UUID eventId);

    // Find events ending soon (for notifications)
    @Query("SELECT e FROM Event e WHERE e.endTime BETWEEN :now AND :soon")
    List<Event> findEventsEndingSoon(@Param("now") LocalDateTime now,
                                     @Param("soon") LocalDateTime soon);
}