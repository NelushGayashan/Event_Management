// src/test/java/com/eventmanagement/repository/UserRepositoryTest.java
package com.eventmanagement.repository;

import com.eventmanagement.config.AuditorAwareConfig;
import com.eventmanagement.config.TestJpaAuditingConfig;
import com.eventmanagement.entity.User;
import com.eventmanagement.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestJpaAuditingConfig.class, AuditorAwareConfig.class})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setRole(Role.USER);
    }

    @Test
    void whenFindByEmail_thenReturnUser() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void whenFindByEmail_withNonExistentEmail_thenReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void whenExistsByEmail_withExistingEmail_thenReturnTrue() {
        entityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void whenExistsByEmail_withNonExistentEmail_thenReturnFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void whenFindByRole_thenReturnUsersWithRole() {
        User adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password123");
        adminUser.setRole(Role.ADMIN);

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(adminUser);

        Page<User> userPage = userRepository.findByRole(Role.USER, PageRequest.of(0, 10));
        Page<User> adminPage = userRepository.findByRole(Role.ADMIN, PageRequest.of(0, 10));

        assertThat(userPage.getContent()).hasSize(1);
        assertThat(userPage.getContent().get(0).getRole()).isEqualTo(Role.USER);

        assertThat(adminPage.getContent()).hasSize(1);
        assertThat(adminPage.getContent().get(0).getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void whenFindByNameContainingOrEmailContaining_thenReturnMatchingUsers() {
        User anotherUser = new User();
        anotherUser.setName("Jane Smith");
        anotherUser.setEmail("jane.smith@example.com");
        anotherUser.setPassword("password123");
        anotherUser.setRole(Role.USER);

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(anotherUser);

        Page<User> foundByName = userRepository.findByNameContainingOrEmailContaining(
                "John", "nonexistent", PageRequest.of(0, 10));
        Page<User> foundByEmail = userRepository.findByNameContainingOrEmailContaining(
                "nonexistent", "jane.smith", PageRequest.of(0, 10));

        assertThat(foundByName.getContent()).hasSize(1);
        assertThat(foundByName.getContent().get(0).getName()).isEqualTo("John Doe");

        assertThat(foundByEmail.getContent()).hasSize(1);
        assertThat(foundByEmail.getContent().get(0).getEmail()).isEqualTo("jane.smith@example.com");
    }
}
