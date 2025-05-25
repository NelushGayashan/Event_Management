// src/test/java/com/eventmanagement/integration/EventManagementIntegrationTest.java
package com.eventmanagement.integration;

import com.eventmanagement.config.AuditorAwareConfig;
import com.eventmanagement.config.TestJpaAuditingConfig;
import com.eventmanagement.dto.request.*;
import com.eventmanagement.dto.response.AuthResponse;
import com.eventmanagement.enums.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestJpaAuditingConfig.class, AuditorAwareConfig.class})
class EventManagementIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;
    private String testEmail;
    private String adminEmail;

    @BeforeEach
    void setUp() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        testEmail = "test_" + System.currentTimeMillis() + "@example.com";
        adminEmail = "admin_" + System.currentTimeMillis() + "@example.com";

        userToken = registerAndLoginUser(testEmail, "Test User", Role.USER);
        adminToken = registerAndLoginUser(adminEmail, "Admin User", Role.ADMIN);
    }

    private String registerAndLoginUser(String email, String name, Role role) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName(name);
        registerRequest.setEmail(email);
        registerRequest.setPassword("password123");
        registerRequest.setRole(role);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(registerRequest))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("password123");

        AuthResponse authResponse = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(loginRequest))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(201)
                .extract()
                .as(AuthResponse.class);

        return authResponse.getAccessToken();
    }

    @Test
    void shouldReturnBadRequestForInvalidRegister() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("");
        registerRequest.setEmail("bademail");
        registerRequest.setPassword("123");
        registerRequest.setRole(Role.USER);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(registerRequest))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("name", notNullValue())
                .body("email", notNullValue())
                .body("password", notNullValue());
    }

    @Test
    void shouldReturnBadRequestForDuplicateRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail(testEmail);
        registerRequest.setPassword("password123");
        registerRequest.setRole(Role.USER);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(registerRequest))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("already registered"));
    }

    @Test
    void shouldReturnBadRequestForInvalidLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("notfound@example.com");
        loginRequest.setPassword("wrongpass");

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(loginRequest))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401), is(500)));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("Test Event");
        createRequest.setDescription("Test Description");
        createRequest.setStartTime(LocalDateTime.now().plusMinutes(5));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(65));
        createRequest.setLocation("Test Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldReturnForbiddenForUserAccessingAdminEndpoints() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(403);
    }

    @Test
    void shouldReturnNotFoundForNonExistentEvent() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/events/" + UUID.randomUUID())
                .then()
                .statusCode(404)
                .body("error", equalTo("Resource Not Found"));
    }

    @Test
    void shouldReturnBadRequestForInvalidUUID() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/events/invalid-uuid")
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Invalid UUID format"));
    }

    @Test
    void shouldLogoutSuccessfully() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body(equalTo("Logged out successfully"));
    }

    @Test
    void shouldCreateEventSuccessfully() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("Integration Test Event");
        createRequest.setDescription("Test Description");
        createRequest.setStartTime(LocalDateTime.now().plusMinutes(5));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(65));
        createRequest.setLocation("Test Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .body("title", equalTo("Integration Test Event"));
    }

    @Test
    void shouldReturnBadRequestForInvalidEvent() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("");
        createRequest.setStartTime(LocalDateTime.now().minusMinutes(10));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(5));
        createRequest.setLocation("");
        createRequest.setVisibility(Visibility.PUBLIC);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldGetUserProfileSuccessfully() {
        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test User"))
                .body("email", equalTo(testEmail))
                .body("role", equalTo("USER"));
    }

    @Test
    void shouldGetAllUsersAsAdminOnly() {
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users")
                .then()
                .statusCode(403);
    }

    @Test
    void shouldUpdateAndDeleteEventSuccessfully() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("Updatable Event");
        createRequest.setDescription("Description");
        createRequest.setStartTime(LocalDateTime.now().plusMinutes(10));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(70));
        createRequest.setLocation("Updatable Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        String eventIdString = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        UUID eventId = UUID.fromString(eventIdString);

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Updated Event");
        updateRequest.setDescription("Updated Description");
        updateRequest.setLocation("Updated Location");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(updateRequest))
                .when()
                .put("/api/events/" + eventId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Event"));

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .delete("/api/events/" + eventId)
                .then()
                .statusCode(204);
    }

    @Test
    void shouldFilterEventsByLocation() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("Filter Event");
        createRequest.setDescription("Filter Description");
        createRequest.setStartTime(LocalDateTime.now().plusMinutes(10));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(70));
        createRequest.setLocation("Filter Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + userToken)
                .queryParam("location", "Filter Location")
                .when()
                .get("/api/events")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldUpdateAttendanceAndGetStatus() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("Attendance Event");
        createRequest.setDescription("Attendance Description");
        createRequest.setStartTime(LocalDateTime.now().plusMinutes(10));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(70));
        createRequest.setLocation("Attendance Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        String eventIdString = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        UUID eventId = UUID.fromString(eventIdString);

        AttendanceRequest attendanceRequest = new AttendanceRequest();
        attendanceRequest.setEventId(eventId);
        attendanceRequest.setStatus(AttendanceStatus.GOING);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(attendanceRequest))
                .when()
                .post("/api/events/attendance")
                .then()
                .statusCode(200)
                .body(equalTo("Attendance updated successfully"));

        String attendanceStatus = given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/events/" + eventId + "/attendance-status")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        String cleanStatus = attendanceStatus.replace("\"", "");
        assertThat(cleanStatus).isEqualTo("GOING");
    }


    @Test
    void shouldReturnUnauthorizedForAttendanceEndpointsWithoutAuth() throws Exception {
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("Attendance Forbidden Event");
        createRequest.setDescription("Description");
        createRequest.setStartTime(LocalDateTime.now().plusMinutes(10));
        createRequest.setEndTime(LocalDateTime.now().plusMinutes(70));
        createRequest.setLocation("Forbidden Location");
        createRequest.setVisibility(Visibility.PUBLIC);

        String eventIdString = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(objectMapper.writeValueAsString(createRequest))
                .when()
                .post("/api/events")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        UUID eventId = UUID.fromString(eventIdString);

        AttendanceRequest attendanceRequest = new AttendanceRequest();
        attendanceRequest.setEventId(eventId);
        attendanceRequest.setStatus(AttendanceStatus.GOING);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(attendanceRequest))
                .when()
                .post("/api/events/attendance")
                .then()
                .statusCode(401);

        given()
                .when()
                .get("/api/events/" + eventId + "/attendance-status")
                .then()
                .statusCode(401);
    }

    @Test
    void shouldHandleAdminOnlyUserOperations() {
        String userIdString = given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        UUID userId = UUID.fromString(userIdString);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users/" + userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.toString()));

        given()
                .header("Authorization", "Bearer " + userToken)
                .when()
                .get("/api/users/" + userId)
                .then()
                .statusCode(403);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/api/users/" + userId)
                .then()
                .statusCode(204);
    }
}
