# Java Spring Boot Skill Set Assessment

## Overview
This document defines the comprehensive skill set for evaluating Java Spring Boot code generation and code review capabilities. It establishes benchmarks for both AI-generated code quality and the ability to review, critique, and improve Spring Boot applications.

---

## 1. Core Spring Boot Fundamentals

### 1.1 Dependency Injection & Beans
**Code Generation Criteria:**
- [ ] Correctly defines Spring beans using `@Bean`, `@Component`, `@Service`, `@Repository`, `@Controller` annotations
- [ ] Implements proper constructor injection over field injection
- [ ] Handles circular dependencies appropriately
- [ ] Uses appropriate bean scopes (Singleton, Prototype, Request, Session)

**Code Review Checklist:**
- Verify injection pattern consistency across codebase
- Identify unnecessary bean creation or lifecycle issues
- Ensure no hardcoded dependencies that should be injected
- Check for proper testing setup with `@SpringBootTest` and `@MockBean`

### 1.2 Application Configuration
**Code Generation Criteria:**
- [ ] Creates proper `application.properties` or `application.yml` files
- [ ] Implements `@Configuration` classes with `@EnableXxx` annotations appropriately
- [ ] Uses Spring Profiles for environment-specific configuration
- [ ] Implements `ConfigurationProperties` for type-safe configuration binding

**Code Review Checklist:**
- Verify configuration externalization (no hardcoded values)
- Check for proper use of `@ConditionalOnProperty`, `@ConditionalOnClass`
- Ensure sensitive data not exposed in config files
- Validate Spring profiles are properly configured

---

## 2. REST API Development

### 2.1 Controller Implementation
**Code Generation Criteria:**
- [ ] Creates controllers with proper `@RestController` or `@Controller` annotations
- [ ] Implements correct HTTP methods (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`)
- [ ] Uses appropriate request/response mapping (`@RequestBody`, `@PathVariable`, `@RequestParam`, `@RequestHeader`)
- [ ] Implements proper request validation with `@Valid`, `@Validated`
- [ ] Returns appropriate HTTP status codes

**Code Review Checklist:**
- Verify RESTful principles are followed (HTTP verbs used correctly)
- Check for input validation on all endpoints
- Ensure proper error handling and exception mapping
- Validate API versioning strategy if present

### 2.2 Exception Handling
**Code Generation Criteria:**
- [ ] Implements `@ControllerAdvice` or `@RestControllerAdvice` for global exception handling
- [ ] Creates custom exception classes extending `RuntimeException`
- [ ] Maps exceptions to appropriate HTTP status codes
- [ ] Provides meaningful error response structures (DTOs)

**Code Review Checklist:**
- Verify consistent error response format across API
- Check for proper logging of exceptions
- Ensure no sensitive information leaked in error messages
- Validate proper use of `@ExceptionHandler`

---

## 3. Data Persistence & ORM

### 3.1 JPA/Hibernate Entities
**Code Generation Criteria:**
- [ ] Creates entities with proper `@Entity`, `@Table` annotations
- [ ] Implements appropriate ID generation strategies (`@Id`, `@GeneratedValue`)
- [ ] Defines relationships correctly (`@OneToOne`, `@OneToMany`, `@ManyToMany`)
- [ ] Uses proper cascading types (CascadeType.PERSIST, MERGE, REMOVE, etc.)
- [ ] Implements lombok annotations appropriately (`@Data`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`)

**Code Review Checklist:**
- Verify N+1 query problem is avoided (use fetch types correctly)
- Check for proper use of `@Transactional` at service layer
- Ensure lazy loading issues are understood and handled
- Validate proper use of `equals()` and `hashCode()` for entities
- Check for proper indexed fields in high-query tables

### 3.2 Repository Layer
**Code Generation Criteria:**
- [ ] Creates repository interfaces extending `JpaRepository`, `CrudRepository`
- [ ] Defines custom query methods with proper naming conventions
- [ ] Uses `@Query` annotations with JPQL/HQL or native SQL when needed
- [ ] Implements pagination and sorting properly (`Pageable`, `Sort`)
- [ ] Uses appropriate projection patterns for read-only queries

**Code Review Checklist:**
- Verify queries are optimized and indexed appropriately
- Check for proper transaction boundaries
- Ensure database-specific queries are documented
- Validate custom repository implementations if present

---

## 4. Service Layer & Business Logic

### 4.1 Service Implementation
**Code Generation Criteria:**
- [ ] Creates service classes with `@Service` annotation
- [ ] Implements `@Transactional` at appropriate layer (read-only vs write)
- [ ] Uses proper exception handling
- [ ] Separates business logic from persistence logic
- [ ] Implements DTOs for data transformation (MapStruct, ModelMapper, or manual mapping)

**Code Review Checklist:**
- Verify transaction boundaries are correct
- Check for proper service method naming conventions
- Ensure no direct entity exposure to controllers (DTO pattern)
- Validate business logic testability

---

## 5. Testing

### 5.1 Unit Testing
**Code Generation Criteria:**
- [ ] Uses JUnit 5 or JUnit 4 with proper test structure
- [ ] Implements Mockito for mocking dependencies
- [ ] Follows Arrange-Act-Assert (AAA) pattern
- [ ] Tests both happy path and error scenarios
- [ ] Achieves reasonable code coverage (>70%)

**Code Review Checklist:**
- Verify test independence (no test order dependencies)
- Check for proper test data setup (builders or fixtures)
- Ensure mocks are used appropriately (not over-mocking)
- Validate meaningful assertions (not just existence checks)

### 5.2 Integration Testing
**Code Generation Criteria:**
- [ ] Uses `@SpringBootTest` for full context tests
- [ ] Implements `@DataJpaTest` for repository layer tests
- [ ] Uses `@WebMvcTest` or `@WebFluxTest` for controller tests
- [ ] Implements proper test database setup (H2, TestContainers)
- [ ] Implements `@Transactional` with `ROLLBACK` for test isolation

**Code Review Checklist:**
- Verify test isolation (rollback after each test)
- Check for proper test fixture usage
- Ensure integration tests don't duplicate unit tests
- Validate external service mocking (WireMock, MockServer)

---

## 6. Security

### 6.1 Authentication & Authorization
**Code Generation Criteria:**
- [ ] Configures Spring Security properly with `@EnableWebSecurity`
- [ ] Implements authentication using JWT, OAuth2, or session-based approach
- [ ] Defines role-based access control with `@PreAuthorize`, `@Secured`
- [ ] Implements proper password encoding (BCrypt, Argon2)
- [ ] Handles CORS configuration appropriately

**Code Review Checklist:**
- Verify authentication mechanism is secure
- Check for proper authorization checks on all endpoints
- Ensure passwords are properly hashed
- Validate CSRF protection is enabled for form-based apps
- Check for SQL injection prevention

### 6.2 Data Security
**Code Generation Criteria:**
- [ ] No hardcoded secrets or credentials in code
- [ ] Uses environment variables or secure vault for secrets
- [ ] Implements proper input validation and sanitization
- [ ] Protects sensitive data in logs (masking)

**Code Review Checklist:**
- Verify no sensitive data in version control
- Check for proper parameterized queries
- Ensure encryption is used for sensitive data in transit/rest
- Validate rate limiting implementation

---

## 7. Performance & Optimization

### 7.1 Caching Strategy
**Code Generation Criteria:**
- [ ] Uses `@Cacheable`, `@CachePut`, `@CacheEvict` annotations appropriately
- [ ] Implements proper cache key strategies
- [ ] Configures cache expiration policies
- [ ] Chooses appropriate cache backend (Redis, Ehcache, etc.)

**Code Review Checklist:**
- Verify cache invalidation strategy is sound
- Check for cache stampede prevention
- Ensure cache keys are unique and appropriate
- Validate cache hit ratio metrics

### 7.2 Query Performance
**Code Generation Criteria:**
- [ ] Uses appropriate fetch strategies (EAGER vs LAZY)
- [ ] Implements query optimization (`@EntityGraph`, projection)
- [ ] Uses pagination for large result sets
- [ ] Implements proper database indexing strategy

**Code Review Checklist:**
- Verify N+1 query problems are avoided
- Check for query execution plans
- Ensure pagination is implemented for list endpoints
- Validate slow query logging is enabled

---

## 8. Logging & Monitoring

### 8.1 Logging Implementation
**Code Generation Criteria:**
- [ ] Uses SLF4J with Logback or Log4j2
- [ ] Implements proper log levels (DEBUG, INFO, WARN, ERROR)
- [ ] Logs relevant business events and errors
- [ ] Avoids sensitive data logging
- [ ] Uses structured logging (JSON format when appropriate)

**Code Review Checklist:**
- Verify log levels are appropriate
- Check for consistent logging patterns
- Ensure no sensitive data in logs
- Validate centralized logging setup (ELK, Splunk, etc.)

### 8.2 Metrics & Monitoring
**Code Generation Criteria:**
- [ ] Uses Spring Boot Actuator endpoints
- [ ] Implements custom metrics with Micrometer
- [ ] Configures health checks appropriately
- [ ] Implements distributed tracing (Spring Cloud Sleuth + Zipkin)

**Code Review Checklist:**
- Verify metrics are meaningful and actionable
- Check for proper monitoring alerts
- Ensure health checks cover critical dependencies
- Validate proper metric naming conventions

---

## 9. Code Quality & Standards

### 9.1 Code Style & Conventions
**Code Generation Criteria:**
- [ ] Follows Java naming conventions (camelCase, PascalCase appropriately)
- [ ] Uses consistent code formatting (indentation, spacing)
- [ ] Implements proper class organization (fields, constructors, methods)
- [ ] Avoids code duplication (DRY principle)
- [ ] Uses appropriate access modifiers (private, protected, public)

**Code Review Checklist:**
- Verify adherence to team code standards
- Check for proper class/method sizing (Single Responsibility Principle)
- Ensure no dead code or unused imports
- Validate proper use of access modifiers

### 9.2 Documentation
**Code Generation Criteria:**
- [ ] Implements JavaDoc for public APIs
- [ ] Creates meaningful comments for complex logic
- [ ] Maintains README with setup instructions
- [ ] Documents API endpoints (Swagger/OpenAPI)
- [ ] Provides configuration documentation

**Code Review Checklist:**
- Verify JavaDoc completeness for public classes/methods
- Check for accurate documentation (not outdated)
- Ensure API documentation is up-to-date
- Validate example code is working

---

## 10. Build & Deployment

### 10.1 Build Configuration
**Code Generation Criteria:**
- [ ] Creates proper `pom.xml` with all required dependencies
- [ ] Implements Maven/Gradle plugin configuration
- [ ] Configures application versioning
- [ ] Sets up build profiles for different environments

**Code Review Checklist:**
- Verify dependency versions are compatible
- Check for unnecessary dependencies
- Ensure build reproducibility
- Validate security vulnerability scanning in build

### 10.2 Deployment Readiness
**Code Generation Criteria:**
- [ ] Implements health checks and readiness probes
- [ ] Configures graceful shutdown
- [ ] Implements proper logging and monitoring setup
- [ ] Creates deployment documentation

**Code Review Checklist:**
- Verify application can start/stop cleanly
- Check for proper environment configuration
- Ensure metrics are exposed for monitoring
- Validate proper error handling during startup

---

## 11. Advanced Spring Boot Topics

### 11.1 Reactive Programming (Spring WebFlux)
**Code Generation Criteria:**
- [ ] Creates reactive controllers with Mono/Flux
- [ ] Implements non-blocking database access (R2DBC)
- [ ] Handles backpressure appropriately
- [ ] Implements proper error handling in reactive chains

**Code Review Checklist:**
- Verify reactive types are used correctly
- Check for blocking operations in reactive code
- Ensure proper subscription management
- Validate error handling in reactive streams

### 11.2 Event-Driven Architecture
**Code Generation Criteria:**
- [ ] Implements Spring Events for internal events
- [ ] Uses message brokers (Kafka, RabbitMQ) appropriately
- [ ] Implements proper event serialization
- [ ] Handles event ordering and idempotency

**Code Review Checklist:**
- Verify event-driven principles are followed
- Check for proper error handling in message processing
- Ensure event schema versioning strategy
- Validate idempotency for critical operations

---

## Scoring Rubric

### Code Generation Quality
- **Excellent (90-100)**: Generates production-ready code with all best practices applied
- **Good (75-89)**: Generates functional code with minor issues
- **Acceptable (60-74)**: Generates working code with some best practices missing
- **Poor (<60)**: Generates code with significant issues or missing critical components

### Code Review Capability
- **Excellent (90-100)**: Identifies all critical issues, provides actionable feedback with solutions
- **Good (75-89)**: Identifies most critical issues with good feedback
- **Acceptable (60-74)**: Identifies some issues with partial feedback
- **Poor (<60)**: Misses critical issues or provides unhelpful feedback

---

## Evaluation Process

1. **Code Generation Test**: Provide requirements and evaluate generated code against criteria
2. **Code Review Test**: Provide Spring Boot code snippets and evaluate review quality
3. **Scenario Analysis**: Present realistic scenarios and evaluate decision-making
4. **Documentation Assessment**: Evaluate clarity and completeness of generated documentation

