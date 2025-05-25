// src/test/java/com/eventmanagement/repository/AttendanceRepositoryTest.java
package com.eventmanagement.repository;

import com.eventmanagement.config.AuditorAwareConfig;
import com.eventmanagement.config.TestJpaAuditingConfig;
import com.eventmanagement.entity.Attendance;
import com.eventmanagement.entity.AttendanceId;
import com.eventmanagement.entity.Event;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.AttendanceStatus;
import com.eventmanagement.enums.Role;
import com.eventmanagement.enums.Visibility;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestJpaAuditingConfig.class, AuditorAwareConfig.class})
class AttendanceRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    private User testUser;
    private Event testEvent;
    private Attendance testAttendance;

    @BeforeEach
    void setUp() {
        super.setUpBaseTest();

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
        testEvent = entityManager.persistAndFlush(testEvent);

        testAttendance = new Attendance();
        testAttendance.setEvent(testEvent);
        testAttendance.setUser(testUser);
        testAttendance.setStatus(AttendanceStatus.GOING);
        testAttendance.setId(new AttendanceId(testEvent.getId(), testUser.getId()));
    }

    @Test
    void whenFindByEventId_thenReturnAttendanceList() {
        entityManager.persistAndFlush(testAttendance);

        List<Attendance> attendances = attendanceRepository.findByEventId(testEvent.getId());

        assertThat(attendances).hasSize(1);
        assertThat(attendances.get(0).getStatus()).isEqualTo(AttendanceStatus.GOING);
        assertThat(attendances.get(0).getUser().getId()).isEqualTo(testUser.getId());
        assertThat(attendances.get(0).getEvent().getId()).isEqualTo(testEvent.getId());
    }

    @Test
    void whenFindByEventIdAndStatus_thenReturnFilteredAttendances() {
        entityManager.persistAndFlush(testAttendance);

        List<Attendance> goingAttendances = attendanceRepository.findByEventIdAndStatus(
                testEvent.getId(), AttendanceStatus.GOING);
        List<Attendance> declinedAttendances = attendanceRepository.findByEventIdAndStatus(
                testEvent.getId(), AttendanceStatus.DECLINED);

        assertThat(goingAttendances).hasSize(1);
        assertThat(goingAttendances.get(0).getStatus()).isEqualTo(AttendanceStatus.GOING);

        assertThat(declinedAttendances).hasSize(0);
    }

    @Test
    void whenExistsByEventIdAndUserId_thenReturnTrue() {
        entityManager.persistAndFlush(testAttendance);

        boolean exists = attendanceRepository.existsByEventIdAndUserId(
                testEvent.getId(), testUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void whenFindByEventIdAndUserId_thenReturnAttendance() {
        entityManager.persistAndFlush(testAttendance);

        Optional<Attendance> found = attendanceRepository.findByEventIdAndUserId(
                testEvent.getId(), testUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(AttendanceStatus.GOING);
    }

    @Test
    void whenCountByEventIdAndStatus_thenReturnCorrectCount() {
        entityManager.persistAndFlush(testAttendance);

        Long count = attendanceRepository.countByEventIdAndStatus(
                testEvent.getId(), AttendanceStatus.GOING);

        assertThat(count).isEqualTo(1L);
    }
}
