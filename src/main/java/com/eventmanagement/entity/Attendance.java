// src/main/java/com/eventmanagement/entity/Attendance.java
package com.eventmanagement.entity;

import com.eventmanagement.enums.AttendanceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Filter(name = "softDeleteFilter", condition = "(:isDeleted = false and deleted_at IS NULL)")
public class Attendance extends BaseEntity {

    @EmbeddedId
    private AttendanceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.GOING;

    @Column(name = "responded_at", nullable = false)
    private LocalDateTime respondedAt;

    public Attendance() {}

    public Attendance(Event event, User user, AttendanceStatus status) {
        this.event = event;
        this.user = user;
        this.status = status;
        this.id = new AttendanceId(event.getId(), user.getId());
    }

    public AttendanceId getId() { return id; }
    public void setId(AttendanceId id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    @PrePersist
    public void prePersist() {
        if (respondedAt == null) {
            respondedAt = LocalDateTime.now();
        }
    }
}
