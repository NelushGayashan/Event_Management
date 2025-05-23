// EventRepositoryTest.java
package com.eventmanagement.repository;

import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.Role;
import com.eventmanagement.enums.Visibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
        testUser = entityManager.persistAndFlush(testUser);

        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setHost(testUser);
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setVisibility(Visibility.PUBLIC);
    }

    @Test
    void whenFindByHostId_thenReturnEventsHostedByUser() {
        entityManager.persistAndFlush(testEvent);

        Page<Event> events = eventRepository.findByHostId(testUser.getId(), PageRequest.of(0, 10));

        assertThat(events.getContent()).hasSize(1);
        assertThat(events.getContent().get(0).getTitle()).isEqualTo("Test Event");
        assertThat(events.getContent().get(0).getHost().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void whenFindByVisibility_thenReturnEventsWithSpecificVisibility() {
        Event privateEvent = new Event();
        privateEvent.setTitle("Private Event");
        privateEvent.setDescription("Private Description");
        privateEvent.setHost(testUser);
        privateEvent.setStartTime(LocalDateTime.now().plusDays(2));
        privateEvent.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2));
        privateEvent.setLocation("Private Location");
        privateEvent.setVisibility(Visibility.PRIVATE);

        entityManager.persistAndFlush(testEvent);
        entityManager.persistAndFlush(privateEvent);

        Page<Event> publicEvents = eventRepository.findByVisibility(Visibility.PUBLIC, PageRequest.of(0, 10));
        Page<Event> privateEvents = eventRepository.findByVisibility(Visibility.PRIVATE, PageRequest.of(0, 10));

        assertThat(publicEvents.getContent()).hasSize(1);
        assertThat(publicEvents.getContent().get(0).getVisibility()).isEqualTo(Visibility.PUBLIC);

        assertThat(privateEvents.getContent()).hasSize(1);
        assertThat(privateEvents.getContent().get(0).getVisibility()).isEqualTo(Visibility.PRIVATE);
    }

    @Test
    void whenFindUpcomingPublicEvents_thenReturnFuturePublicEvents() {
        Event pastEvent = new Event();
        pastEvent.setTitle("Past Event");
        pastEvent.setDescription("Past Description");
        pastEvent.setHost(testUser);
        pastEvent.setStartTime(LocalDateTime.now().minusDays(1));
        pastEvent.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));
        pastEvent.setLocation("Past Location");
        pastEvent.setVisibility(Visibility.PUBLIC);

        entityManager.persistAndFlush(testEvent);
        entityManager.persistAndFlush(pastEvent);

        Page<Event> upcomingEvents = eventRepository.findUpcomingPublicEvents(
                LocalDateTime.now(), PageRequest.of(0, 10));

        assertThat(upcomingEvents.getContent()).hasSize(1);
        assertThat(upcomingEvents.getContent().get(0).getTitle()).isEqualTo("Test Event");
        assertThat(upcomingEvents.getContent().get(0).getStartTime()).isAfter(LocalDateTime.now());
    }

    @Test
    void whenFindByDateRange_thenReturnEventsInRange() {
        LocalDateTime rangeStart = LocalDateTime.now().plusDays(1);
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(3);

        Event eventInRange = new Event();
        eventInRange.setTitle("Event In Range");
        eventInRange.setDescription("Description");
        eventInRange.setHost(testUser);
        eventInRange.setStartTime(LocalDateTime.now().plusDays(2));
        eventInRange.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2));
        eventInRange.setLocation("Location");
        eventInRange.setVisibility(Visibility.PUBLIC);

        Event eventOutOfRange = new Event();
        eventOutOfRange.setTitle("Event Out Of Range");
        eventOutOfRange.setDescription("Description");
        eventOutOfRange.setHost(testUser);
        eventOutOfRange.setStartTime(LocalDateTime.now().plusDays(5));
        eventOutOfRange.setEndTime(LocalDateTime.now().plusDays(5).plusHours(2));
        eventOutOfRange.setLocation("Location");
        eventOutOfRange.setVisibility(Visibility.PUBLIC);

        entityManager.persistAndFlush(eventInRange);
        entityManager.persistAndFlush(eventOutOfRange);

        Page<Event> eventsInRange = eventRepository.findByDateRange(
                rangeStart, rangeEnd, PageRequest.of(0, 10));

        assertThat(eventsInRange.getContent()).hasSize(1);
        assertThat(eventsInRange.getContent().get(0).getTitle()).isEqualTo("Event In Range");
    }

    @Test
    void whenFindByLocationContainingIgnoreCase_thenReturnEventsWithMatchingLocation() {
        Event eventInNewYork = new Event();
        eventInNewYork.setTitle("NYC Event");
        eventInNewYork.setDescription("Description");
        eventInNewYork.setHost(testUser);
        eventInNewYork.setStartTime(LocalDateTime.now().plusDays(1));
        eventInNewYork.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        eventInNewYork.setLocation("New York City");
        eventInNewYork.setVisibility(Visibility.PUBLIC);

        entityManager.persistAndFlush(testEvent); // "Test Location"
        entityManager.persistAndFlush(eventInNewYork);

        Page<Event> eventsInNY = eventRepository.findByLocationContainingIgnoreCase(
                "new york", PageRequest.of(0, 10));
        Page<Event> eventsInTest = eventRepository.findByLocationContainingIgnoreCase(
                "test", PageRequest.of(0, 10));

        assertThat(eventsInNY.getContent()).hasSize(1);
        assertThat(eventsInNY.getContent().get(0).getLocation()).containsIgnoringCase("New York");

        assertThat(eventsInTest.getContent()).hasSize(1);
        assertThat(eventsInTest.getContent().get(0).getLocation()).containsIgnoringCase("Test");
    }

    @Test
    void whenFindEventsWithFilters_thenReturnFilteredEvents() {
        entityManager.persistAndFlush(testEvent);

        Page<Event> filteredEvents = eventRepository.findEventsWithFilters(
                "Test", "Test Location", null, null, Visibility.PUBLIC, testUser.getId(),
                PageRequest.of(0, 10));

        assertThat(filteredEvents.getContent()).hasSize(1);
        assertThat(filteredEvents.getContent().get(0).getTitle()).isEqualTo("Test Event");
    }

    @Test
    void whenFindEventWithAttendeeCount_thenReturnEventWithCount() {
        Event savedEvent = entityManager.persistAndFlush(testEvent);

        Optional<Object[]> result = eventRepository.findEventWithAttendeeCount(savedEvent.getId());

        assertThat(result).isPresent();
        Object[] eventWithCount = result.get();
        Event event = (Event) eventWithCount[0];
        Long count = (Long) eventWithCount[1];

        assertThat(event.getTitle()).isEqualTo("Test Event");
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void whenFindEventsEndingSoon_thenReturnEventsEndingInTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soon = now.plusHours(1);

        Event endingSoonEvent = new Event();
        endingSoonEvent.setTitle("Ending Soon Event");
        endingSoonEvent.setDescription("Description");
        endingSoonEvent.setHost(testUser);
        endingSoonEvent.setStartTime(now.minusHours(1));
        endingSoonEvent.setEndTime(now.plusMinutes(30)); // Ends in 30 minutes
        endingSoonEvent.setLocation("Location");
        endingSoonEvent.setVisibility(Visibility.PUBLIC);

        entityManager.persistAndFlush(testEvent); // Ends in 1 day + 2 hours
        entityManager.persistAndFlush(endingSoonEvent);

        List<Event> endingSoonEvents = eventRepository.findEventsEndingSoon(now, soon);

        assertThat(endingSoonEvents).hasSize(1);
        assertThat(endingSoonEvents.get(0).getTitle()).isEqualTo("Ending Soon Event");
    }
}
