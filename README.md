# Event Management System (Backend)

A production-ready RESTful API for an Event Management Platform built with Spring Boot 3.3.0, featuring secure access, comprehensive filtering capabilities, and scalable architecture.

---

📋 **Table of Contents**

- [Project Overview](#-project-overview)
- [Features](#-features)
- [Architecture](#️-architecture)
- [Database Schema](#-database-schema)
- [API Endpoints](#api-endpoints)
- [Folder Structure](#folder-structure)
- [Key Code Files](#key-code-files)
- [Exception Handling](#exception-handling)
- [Dependencies](#dependencies)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Security](#security)
- [Performance Features](#performance-features)
- [Testing](#testing)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Deployment](#deployment)
- [API Documentation](#api-documentation)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Project Overview

The Event Management System is a comprehensive backend solution designed to handle all aspects of event planning and management. This system provides a robust RESTful API that enables users to create, manage, and attend events with full authentication and authorization capabilities.

### Key Highlights

- **Scalable Architecture**: Built with Spring Boot 3.x and modern design patterns  
- **Security First**: JWT-based authentication with role-based access control  
- **Performance Optimized**: Caching, pagination, and database optimizations  
- **Production Ready**: Comprehensive testing, monitoring, and deployment configurations  
- **Enterprise Grade**: Soft deletes, audit trails, and advanced filtering capabilities  

### Business Value

- Streamlines event management workflows  
- Provides secure multi-tenant event hosting  
- Enables real-time attendance tracking  
- Supports complex event filtering and search  
- Offers comprehensive administrative controls  

---


## 🚀 Features

### Core Functionality

- **User Management**: Registration, authentication, and role-based access control  
- **Event Management**: Create, update, delete, and manage events with advanced filtering  
- **Attendance Tracking**: RSVP system with status tracking (`GOING`, `MAYBE`, `DECLINED`)  
- **Security**: JWT-based authentication with role-based authorization  
- **Soft Deletes**: Archive entities instead of permanent deletion  

### Advanced Features

- **Caching**: Caffeine cache for improved performance  
- **Rate Limiting**: Request throttling per user/IP using Bucket4j  
- **Pagination & Filtering**: Advanced search and sorting capabilities  
- **Audit Trail**: Automatic tracking of creation and modification timestamps  
- **Database Migrations**: Flyway for version-controlled schema management  

---


## 🏗️ Architecture

### Tech Stack

- **Java 17**  
- **Spring Boot 3.3.0**  
- **Spring Security 6 with JWT**  
- **Spring Data JPA with Hibernate**  
- **PostgreSQL**  
- **Maven**  
- **Caffeine Caching**  
- **MapStruct**  
- **Flyway**  

### Design Patterns

- **Layered Architecture** (`Controller → Service → Repository`)  
- **Repository Pattern** with Spring Data JPA  
- **Builder Pattern** for complex object creation  
- **Strategy Pattern** for filtering mechanisms  
- **Factory Pattern** for user principal creation  

---


## 📊 Database Schema

Core Entities

User
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
```
Event
```sql
CREATE TABLE events (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    host_id UUID NOT NULL REFERENCES users(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(500) NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);
Attendance
sql
CREATE TABLE attendance (
    event_id UUID NOT NULL REFERENCES events(id),
    user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'GOING',
    responded_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    PRIMARY KEY (event_id, user_id)
);
```


### 🔌 API Endpoints

### Authentication
| Method | Endpoint            | Description       | Auth Required |
|--------|---------------------|-------------------|----------------|
| POST   | /api/auth/register  | User registration | No             |
| POST   | /api/auth/login     | User login        | No             |
| POST   | /api/auth/logout    | User logout       | Yes            |

### Events
| Method | Endpoint                    | Description                 | Auth Required      |
|--------|-----------------------------|-----------------------------|--------------------|
| POST   | /api/events                 | Create event                | Yes (USER/ADMIN)   |
| GET    | /api/events                 | List events with filters    | No                 |
| GET    | /api/events/{id}            | Get event details           | No                 |
| PUT    | /api/events/{id}            | Update event                | Yes (Host/ADMIN)   |
| DELETE | /api/events/{id}            | Delete event                | Yes (Host/ADMIN)   |
| GET    | /api/events/upcoming        | Get upcoming events         | No                 |
| GET    | /api/events/my-events       | Get user's hosted events    | Yes                |
| GET    | /api/events/attending       | Get user's attending events | Yes                |


### Attendance
| Method | Endpoint                                | Description                   | Auth Required  |
|--------|-----------------------------------------|-------------------------------|----------------|
| POST   | /api/events/attendance                  | Update attendance status      | Yes            |
| GET    | /api/events/{id}/attendance-status      | Get user's attendance status  | Yes            |


Users
| Method | Endpoint            | Description              | Auth Required  |
|--------|---------------------|--------------------------|----------------|
| GET    | /api/users/me       | Get current user profile | Yes            |
| GET    | /api/users          | Get all users            | Yes (ADMIN)    |
| GET    | /api/users/{id}     | Get user by ID           | Yes (ADMIN)    |
| DELETE | /api/users/{id}     | Deactivate user          | Yes (ADMIN)    |



## 📁 Folder Structure

```
event-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/eventmanagement/
│   │   │   ├── config/                 # Application configuration classes
│   │   │   │   ├── AuditorAwareConfig.java
│   │   │   │   ├── CacheConfig.java
│   │   │   │   ├── HibernateConfig.java
│   │   │   │   ├── JpaAuditingConfig.java
│   │   │   │   ├── JpaConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── MethodSecurityConfig.java
│   │   │   │   ├── RateLimitConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SoftDeleteFilterConfig.java
│   │   │   ├── controller/             # REST API controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── EventController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                    # Data Transfer Objects (request/response)
│   │   │   │   ├── request/
│   │   │   │   │   ├── AttendanceRequest.java
│   │   │   │   │   ├── CreateEventRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   └── UpdateEventRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── EventDetailResponse.java
│   │   │   │       ├── EventResponse.java
│   │   │   │       ├── EventWithAttendeeCountResponse.java
│   │   │   │       ├── PagedResponse.java
│   │   │   │       └── UserResponse.java
│   │   │   ├── entity/                 # JPA entities (database models)
│   │   │   │   ├── Attendance.java
│   │   │   │   ├── AttendanceId.java
│   │   │   │   ├── BaseEntity.java
│   │   │   │   ├── Event.java
│   │   │   │   └── User.java
│   │   │   ├── enums/                  # Enumeration classes
│   │   │   │   ├── AttendanceStatus.java
│   │   │   │   ├── Role.java
│   │   │   │   └── Visibility.java
│   │   │   ├── exception/              # Custom exceptions and global handler
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   ├── mapper/                 # MapStruct interfaces for DTO-entity mapping
│   │   │   │   ├── EventMapper.java
│   │   │   │   └── UserMapper.java
│   │   │   ├── repository/             # Spring Data JPA repositories
│   │   │   │   ├── AttendanceRepository.java
│   │   │   │   ├── EventRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/               # Security components (JWT, UserDetails)
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── UserPrincipal.java
│   │   │   ├── service/                # Business logic interfaces
│   │   │   │   ├── impl/
│   │   │   │   │   ├── AuthServiceImpl.java
│   │   │   │   │   ├── EventServiceImpl.java
│   │   │   │   │   └── UserServiceImpl.java
│   │   │   │   ├── AttendanceService.java
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── EventService.java
│   │   │   │   ├── FilterService.java
│   │   │   │   ├── SoftDeleteService.java
│   │   │   │   └── UserService.java
│   │   │   ├── util/                   # Utility classes
│   │   │   │   ├── DateUtils.java
│   │   │   │   └── ValidationUtils.java
│   │   │   └── EventManagementApplication.java # Main Spring Boot application class
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/           # Flyway database migration scripts
│   │           ├── V1__Create_users_table.sql
│   │           ├── V2__Create_events_table.sql
│   │           └── V3__Create_attendance_table.sql
│   └── test/
│       ├── java/com/eventmanagement/
│       │   ├── config/                 # Test-specific configurations
│       │   │   ├── BaseWebMvcTest.java
│       │   │   ├── TestJpaAuditingConfig.java
│       │   │   ├── TestJpaConfig.java
│       │   │   ├── TestMethodSecurityConfig.java
│       │   │   ├── TestSecurityBeans.java
│       │   │   └── TestSecurityConfig.java
│       │   ├── controller/             # Unit tests for controllers
│       │   │   ├── AuthControllerTest.java
│       │   │   ├── EventControllerTest.java
│       │   │   └── UserControllerTest.java
│       │   ├── integration/            # Integration tests
│       │   │   └── EventManagementIntegrationTest.java
│       │   ├── repository/             # Unit tests for repositories
│       │   │   ├── AttendanceRepositoryTest.java
│       │   │   ├── BaseRepositoryTest.java
│       │   │   ├── EventRepositoryTest.java
│       │   │   └── UserRepositoryTest.java
│       │   ├── security/               # Unit tests for security components
│       │   │   └── JwtTokenProviderTest.java
│       │   └── service/                # Unit tests for services
│       │       ├── AuthServiceTest.java
│       │       ├── EventServiceTest.java
│       │       ├── TestFilterService.java
│       │       └── UserServiceTest.java
│       │   └── util/                   # Unit tests for utility classes
│       │       ├── DateUtilsTest.java
│       │       └── ValidationUtilsTest.java
│       ├── EventManagementApplicationTests.java # Main Spring Boot test class
│       └── resources/
│           └── application-test.yml    # Test-specific application properties
├── target/                             # Build output directory
├── pom.xml                             # Maven project configuration
├── README.md                           # Project documentation
└── .gitignore                          # Git ignore rules
```
![Screenshot from 2025-05-25 23-34-23](https://github.com/user-attachments/assets/9a269265-5513-4f30-80d6-af4c92edcaf0)



## 🔑 Key Code Files

### 1. EventController.java  
**Location:** `src/main/java/com/eventmanagement/controller/EventController.java`

The main REST controller handling all event-related operations. This file demonstrates:

- RESTful API design with proper HTTP methods and status codes  
- Method-level security with `@PreAuthorize` annotations  
- Request validation using `@Valid` annotations  
- Pagination and filtering support  
- Comprehensive CRUD operations  

**Key Features:**

- Event creation, updating, and deletion with authorization checks  
- Advanced filtering by date, location, visibility, and host  
- Pagination support for large datasets  
- Attendance management integration  

---

### 2. EventServiceImpl.java  
**Location:** `src/main/java/com/eventmanagement/service/impl/EventServiceImpl.java`

The core business logic implementation for event management. This file showcases:

- Clean separation of concerns between controller and business logic  
- Caching strategies with `@Cacheable` and `@CacheEvict`  
- Soft delete implementation  
- Complex business rule validation  
- Transaction management  

**Key Features:**

- Event validation (future dates, end time after start time)  
- Permission checking (host or admin only for modifications)  
- Soft delete filter management  
- Attendance statistics calculation  
- Cache management for performance optimization  

---

### 3. SecurityConfig.java  
**Location:** `src/main/java/com/eventmanagement/config/SecurityConfig.java`

Comprehensive security configuration demonstrating:

- JWT-based authentication setup  
- Role-based access control configuration  
- CORS policy management  
- Security filter chain configuration  
- Password encoding setup  

**Key Features:**

- Stateless session management  
- Custom JWT authentication filter  
- Public and protected endpoint configuration  
- Security exception handling  

---

### 4. GlobalExceptionHandler.java  
**Location:** `src/main/java/com/eventmanagement/exception/GlobalExceptionHandler.java`

Centralized exception handling showcasing:

- Global exception handling with `@RestControllerAdvice`  
- Custom exception types for different error scenarios  
- Proper HTTP status code mapping  
- Validation error handling with field-specific messages  
- Structured error response format  

**Key Features:**

- Resource not found handling (404)  
- Validation error handling (400)  
- Authorization error handling (401/403)  
- UUID format validation  
- Generic exception fallback  

---

### 5. EventManagementIntegrationTest.java  
**Location:** `src/test/java/com/eventmanagement/integration/EventManagementIntegrationTest.java`

Comprehensive integration testing demonstrating:

- End-to-end API testing with RestAssured  
- Authentication flow testing  
- All CRUD operations testing  
- Exception scenario testing  
- Security testing for different user roles  

**Key Features:**

- Complete test coverage for all endpoints  
- Authentication and authorization testing  
- Validation error testing  
- Status code verification  
- Response body validation  



## ⚠️ Exception Handling

The Event Management System implements a comprehensive exception handling strategy that ensures robust error management and meaningful error responses to clients.

### Exception Architecture

#### Custom Exception Classes
The system defines specific exception types for different error scenarios:

```java
// Base custom exceptions
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

### Global Exception Handler
The GlobalExceptionHandler class provides centralized exception handling using @RestControllerAdvice:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle resource not found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle bad requests (400)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle unauthorized access (401)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handle access denied (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                "You don't have permission to access this resource",
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Handle validation errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Handle invalid UUID format (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        if (ex.getRequiredType() == java.util.UUID.class) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "Invalid UUID format",
                    request.getDescription(false).replace("uri=", ""),
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        return handleGlobalException(ex, request);
    }

    // Handle all other exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", ""),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### Error Response Structure
The system uses a standardized error response format:

```java
public static class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    
    // Constructors, getters, and setters
}
```

### Exception Scenarios Handled

1. **Validation Errors (400 Bad Request)**  
   - **Trigger:** Invalid request data (empty fields, invalid email format, etc.)  
   - **Response:** Field-specific validation error messages  

Example:
```json
{
    "name": "Name cannot be blank",
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters"
}
```

2. **Resource Not Found (404 Not Found)**  
   - **Trigger:** Requesting non-existent resources  
   - **Response:** Structured error with resource details  

Example:
```json
{
    "status": 404,
    "error": "Resource Not Found",
    "message": "Event not found with id: '123e4567-e89b-12d3-a456-426614174000'",
    "path": "/api/events/123e4567-e89b-12d3-a456-426614174000",
    "timestamp": "2024-01-15T10:30:00"
}
```

3. **Unauthorized Access (401 Unauthorized)**  
   - **Trigger:** Missing or invalid JWT token  
   - **Response:** Unauthorized error message  

Example:
```json
{
    "status": 401,
    "error": "Unauthorized",
    "message": "JWT token is expired or invalid",
    "path": "/api/events",
    "timestamp": "2024-01-15T10:30:00"
}
```

4. **Forbidden Access (403 Forbidden)**  
   - **Trigger:** Insufficient permissions for the requested operation  
   - **Response:** Access denied message  

Example:
```json
{
    "status": 403,
    "error": "Access Denied",
    "message": "You don't have permission to access this resource",
    "path": "/api/users",
    "timestamp": "2024-01-15T10:30:00"
}
```

5. **Business Logic Errors (400 Bad Request)**  
   - **Trigger:** Business rule violations (e.g., event start time in the past)  
   - **Response:** Descriptive business error message  

Example:
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Event start time must be in the future",
    "path": "/api/events",
    "timestamp": "2024-01-15T10:30:00"
}
```

6. **Invalid UUID Format (400 Bad Request)**  
   - **Trigger:** Malformed UUID in path parameters  
   - **Response:** UUID format error message  

Example:
```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid UUID format",
    "path": "/api/events/invalid-uuid",
    "timestamp": "2024-01-15T10:30:00"
}
```

### Exception Testing
The system includes comprehensive exception testing in integration tests:

```java
@Test
void shouldReturnBadRequestForInvalidRegister() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setName(""); // Invalid name
    registerRequest.setEmail("bademail"); // Invalid email
    registerRequest.setPassword("123"); // Invalid password

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
void shouldReturnForbiddenForUserAccessingAdminEndpoints() {
    given()
        .header("Authorization", "Bearer " + userToken)
        .when()
        .get("/api/users")
        .then()
        .statusCode(403);
}
```

### Benefits of This Exception Handling Strategy

- **Consistent Error Responses:** All errors follow the same structure  
- **Meaningful Error Messages:** Clear, actionable error descriptions  
- **Proper HTTP Status Codes:** Correct status codes for different error types  
- **Security:** No sensitive information leaked in error messages  
- **Debugging Support:** Comprehensive logging for internal errors  
- **Client-Friendly:** Structured responses that frontend applications can easily parse  
- **Comprehensive Coverage:** Handles all common error scenarios  

This exception handling approach ensures that the API provides clear, consistent, and helpful error responses while maintaining security and system stability.



## 📦 Dependencies

### Core Spring Boot Dependencies
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
    <relativePath/>
</parent>

<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Database Dependencies
```xml
<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Flyway Migration -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>

<!-- H2 Database for Testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### Security Dependencies
```xml
<!-- JWT Support -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### Performance & Caching Dependencies
```xml
<!-- Caffeine Cache -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

### Mapping & Utilities
```xml
<!-- MapStruct for DTO Mapping -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

### Testing Dependencies
```xml
<!-- Spring Boot Test Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- RestAssured for Integration Testing -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers for Database Testing -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

### Build Plugins
```xml
<build>
    <plugins>
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        
        <!-- Maven Compiler Plugin -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        
        <!-- Surefire Plugin for Testing -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
        </plugin>
        
        <!-- JaCoCo for Code Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.8</version>
        </plugin>
    </plugins>
</build>
```


## 🔧 Setup & Installation

### Prerequisites

- Java 17 or higher  
- PostgreSQL 12+  
- Maven 3.6+  

### Environment Variables
```bash
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export DATABASE_URL=jdbc:postgresql://localhost:5432/event_management_dev
export JWT_SECRET=your_jwt_secret_key_minimum_256_bits
```

### Database Setup
```sql
-- Create database
CREATE DATABASE event_management_dev;

-- Create user (optional)
CREATE USER event_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE event_management_dev TO event_user;
```

### Running the Application
1. Clone the repository
```bash
git clone https://github.com/NelushGayashan/Event_Management
cd event-management-system
```

2. Install dependencies
```bash
./mvnw clean install
```

3. Run the application
```bash
./mvnw spring-boot:run
```
The application will start on http://localhost:8080

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=EventManagementIntegrationTest

# Run with coverage
./mvnw test jacoco:report
```

### 📝 Configuration

#### Application Profiles

**Development (`application-dev.yml`)**

- Detailed logging  
- Database DDL auto-update  
- Debug mode enabled  
- H2 console access  

**Production (`application-prod.yml`)**

- Optimized logging  
- Connection pooling  
- SSL requirements  
- Performance monitoring  

**Test (`application-test.yml`)**

- H2 in-memory database  
- JPA auditing enabled  
- Hardcoded JWT secret  

#### Key Configuration Properties
```text
# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000 # 24 hours

# Database Configuration
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# Caching Configuration
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=10m
```


## 🔒 Security

### Authentication Flow

- User registers/logs in with credentials  
- Server validates credentials and generates JWT token  
- Client includes JWT token in `Authorization: Bearer <token>` header  
- Server validates token for protected endpoints  

### Role-Based Access Control

- **USER:** Can create events, manage own events, update attendance  
- **ADMIN:** Full access to all resources, user management  

### Security Features

- Password encryption using BCrypt  
- JWT token validation and blacklisting  
- Method-level security with `@PreAuthorize`  
- CORS configuration for frontend integration  
- Request rate limiting  



## 📊 Performance Features

### Caching Strategy
```java
@Cacheable(value = "events", key = "#eventId + '_' + #userId")
public EventDetailResponse getEventDetails(UUID eventId, UUID userId) {
    // Implementation
}

@CacheEvict(value = "events", allEntries = true)
public EventResponse createEvent(CreateEventRequest request, UUID userId) {
    // Implementation
}
```

### Database Optimizations

- Proper indexing on frequently queried columns  
- Lazy loading for entity relationships  
- Connection pooling with HikariCP  
- Query optimization with custom JPQL  

## Soft Delete Implementation
```java
@Filter(name = "softDeleteFilter", condition = "(:isDeleted = false and deleted_at IS NULL)")
public class BaseEntity {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public void softDelete() { 
        this.deletedAt = LocalDateTime.now(); 
    }
}
```


## 🧪 Testing

### Test Coverage

- Unit Tests: Controller, Service, and Repository layers  
- Integration Tests: End-to-end API testing with RestAssured  
- Security Tests: Authentication and authorization scenarios  
- Exception Handling Tests: Global exception handler coverage  

### Test Structure
```text
src/test/java/
├── controller/          # Controller unit tests
├── service/            # Service layer tests
├── repository/         # Repository tests with H2
├── integration/        # Full integration tests
└── config/            # Test configuration
```

### Sample Test
```java
@Test
void shouldCreateEventSuccessfully() throws Exception {
    CreateEventRequest createRequest = new CreateEventRequest();
    createRequest.setTitle("Integration Test Event");
    // ... set other fields

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
```


## 📈 Monitoring & Health Checks

### Actuator Endpoints

- `/actuator/health` - Application health status  
- `/actuator/info` - Application information  
- `/actuator/metrics` - Application metrics  

### Logging Configuration
```text
logging:
  level:
    com.eventmanagement: INFO
    org.springframework.security: WARN
  file:
    name: logs/event-management.log
    max-size: 100MB
    max-history: 30
```


## 🚀 Deployment
Environment-Specific Deployment
```bash
# Development
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Production
java -jar target/event-management-system-*.jar --spring.profiles.active=prod
```


## 📋 API Documentation

### Request/Response Examples

- Create Event
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tech Conference 2024",
    "description": "Annual technology conference",
    "startTime": "2024-06-15T09:00:00",
    "endTime": "2024-06-15T17:00:00",
    "location": "Convention Center",
    "visibility": "PUBLIC"
  }'
```

- Filter Events
```bash
curl "http://localhost:8080/api/events?location=Convention&startDate=2024-06-01T00:00:00&visibility=PUBLIC&page=0&size=10"
```


## 🐛 Troubleshooting

### Common Issues
- JWT Token Issues
```text
Error: 401 Unauthorized
Solution: Ensure JWT_SECRET environment variable is set and token is valid
```

- Database Connection Issues
```text
Error: Connection refused
Solution: Verify PostgreSQL is running and credentials are correct
```

- Soft Delete Filter Issues
```text
Error: Deleted entities still appearing
Solution: Ensure FilterService.enableSoftDeleteFilter() is called
```


## 🤝 Contributing

- Fork the repository  
- Create a feature branch (`git checkout -b feature/amazing-feature`)  
- Commit your changes (`git commit -m 'Add amazing feature'`)  
- Push to the branch (`git push origin feature/amazing-feature`)  
- Open a Pull Request  

### Code Standards

- Follow Java naming conventions  
- Write comprehensive tests for new features  
- Update documentation for API changes  
- Ensure all tests pass before submitting PR  

---


## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---


## 👥 Authors

Nelush Gayashan - [https://github.com/NelushGayashan](https://github.com/NelushGayashan)

---


## 🙏 Acknowledgments

- Spring Boot team for the excellent framework  
- Hibernate team for ORM capabilities  
- JWT.io for token standards  
- All contributors who helped improve this project  

Built with ❤️ using Spring Boot 3.3.0
