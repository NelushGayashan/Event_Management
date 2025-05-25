// src/main/java/com/eventmanagement/dto/response/EventWithAttendeeCountResponse.java
package com.eventmanagement.dto.response;

import com.eventmanagement.entity.Event;

public class EventWithAttendeeCountResponse {
    private final Event event;
    private final long attendeeCount;

    public EventWithAttendeeCountResponse(Event event, long attendeeCount) {
        this.event = event;
        this.attendeeCount = attendeeCount;
    }

    public Event getEvent() {
        return event;
    }

    public long getAttendeeCount() {
        return attendeeCount;
    }
}
