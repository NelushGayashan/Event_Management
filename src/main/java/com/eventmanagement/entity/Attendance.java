package com.eventmanagement.entity;

import com.eventmanagement.enums.AttendanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

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
    @NotNull
    @Column(nullable = false)
    private AttendanceStatus status = AttendanceStatus.GOING;

    @CreationTimestamp
    @Column(name = "responded_at", nullable = false)
    private LocalDateTime respondedAt;

    // Constructors
    public Attendance() {}

    public Attendance(Event event, User user, AttendanceStatus status) {
        this.event = event;
        this.user = user;
        this.status = status;
        this.id = new AttendanceId(event.getId(), user.getId());
    }

    // Getters and Setters
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
}