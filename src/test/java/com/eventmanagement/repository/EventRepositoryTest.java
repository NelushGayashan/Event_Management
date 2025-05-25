// src/test/java/com/eventmanagement/repository/EventRepositoryTest.java
package com.eventmanagement.repository;

import com.eventmanagement.config.AuditorAwareConfig;
import com.eventmanagement.config.TestJpaAuditingConfig;
import com.eventmanagement.dto.response.EventWithAttendeeCountResponse;
import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Role;
import com.eventmanagement.enums.Visibility;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
@Import({TestJpaAuditingConfig.class, AuditorAwareConfig.class})
class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();

        eventRepository.deleteAll();
        userRepository.deleteAll();
        attendanceRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();

        testUser = new User();
        testUser.setName("Host User");
        testUser.setEmail("host@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    @Order(9)
    void whenFindEventWithAttendeeCount_thenReturnEventWithCount() {
        Session session = entityManager.getEntityManager().unwrap(Session.class);
        session.enableFilter("softDeleteFilter").setParameter("isDeleted", false);

        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        Event testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setHost(testUser);
        testEvent.setStartTime(futureStart);
        testEvent.setEndTime(futureStart.plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setVisibility(Visibility.PUBLIC);
        Event savedEvent = entityManager.persistAndFlush(testEvent);

        Attendance attendance = new Attendance(savedEvent, testUser, AttendanceStatus.GOING);
        attendance.setRespondedAt(LocalDateTime.now());
        attendanceRepository.save(attendance);

        entityManager.flush();
        entityManager.clear();

        Optional<EventWithAttendeeCountResponse> result = eventRepository.findEventWithAttendeeCount(savedEvent.getId());

        assertThat(result).isPresent();
        EventWithAttendeeCountResponse eventWithCount = result.get();
        assertThat(eventWithCount.getEvent().getTitle()).isEqualTo("Test Event");
        assertThat(eventWithCount.getAttendeeCount()).isEqualTo(1L);
    }
}
