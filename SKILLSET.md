# Java Spring Boot Industry Standard Skill Set

## Overview
This is an **industry-standard** skill set aligned with professional Spring Boot development practices. It covers the most commonly required capabilities in enterprise environments and is designed to be applicable across diverse project types (REST APIs, microservices, event-driven systems, etc.).

---

## 1. REST API Development & Web Controllers

### 1.1 RESTful Endpoint Design
**Generation Criteria:**
- [ ] Uses appropriate HTTP verbs (GET, POST, PUT, DELETE, PATCH)
- [ ] Implements proper status codes (200, 201, 204, 400, 401, 403, 404, 409, 500)
- [ ] Designs RESTful URLs following conventions (`/api/v1/resources/{id}`)
- [ ] Implements request validation using `@Valid`, `@Validated`
- [ ] Returns consistent response DTOs with metadata

**Review Checklist:**
- ✓ HTTP methods match CRUD operations correctly
- ✓ All endpoints validate input properly
- ✓ Status codes are semantically correct
- ✓ API versioning strategy is consistent
- ✓ Pagination implemented for list endpoints

### 1.2 Exception Handling & Error Responses
**Generation Criteria:**
- [ ] Implements `@RestControllerAdvice` for global exception handling
- [ ] Creates custom exception classes for business errors
- [ ] Maps exceptions to appropriate HTTP status codes
- [ ] Returns standardized error response format (with error code, message, timestamp)
- [ ] Logs exceptions appropriately without exposing sensitive data

**Review Checklist:**
- ✓ All exceptions caught and mapped to HTTP responses
- ✓ Error response includes error code, message, and timestamp
- ✓ No stack traces exposed to clients
- ✓ Consistent error response structure across all endpoints
- ✓ Proper logging of errors for debugging

---

## 2. Dependency Injection & Component Lifecycle

### 2.1 Bean Definition & Injection Patterns
**Generation Criteria:**
- [ ] Uses constructor injection (preferred) over field injection
- [ ] Defines beans with appropriate annotations (`@Component`, `@Service`, `@Repository`)
- [ ] Implements proper bean scopes (Singleton, Prototype)
- [ ] Avoids circular dependencies or resolves them properly
- [ ] Uses `@Qualifier` or `@Primary` for multiple bean implementations

**Review Checklist:**
- ✓ Constructor injection used consistently (not @Autowired on fields)
- ✓ Bean annotations applied correctly based on responsibility
- ✓ No unnecessary bean creation
- ✓ Beans properly initialized and destroyed
- ✓ Dependencies clearly visible in constructor

### 2.2 Configuration Management
**Generation Criteria:**
- [ ] Externalizes configuration using `application.yml` or `application.properties`
- [ ] Implements environment-specific profiles (dev, test, prod)
- [ ] Uses `@ConfigurationProperties` for type-safe configuration
- [ ] Avoids hardcoded values in code
- [ ] Manages secrets via environment variables or secure vault

**Review Checklist:**
- ✓ All configuration externalized from code
- ✓ Sensitive data not in version control
- ✓ Spring profiles properly configured
- ✓ Configuration validates on startup
- ✓ Default values provided with override capability

---

## 3. Data Persistence & Transactions

### 3.1 Repository Pattern Implementation
**Generation Criteria:**
- [ ] Extends `JpaRepository` or `CrudRepository` for data access
- [ ] Implements custom query methods with proper naming conventions
- [ ] Uses `@Query` annotation for complex queries (JPQL, native SQL)
- [ ] Implements pagination with `Pageable` and `Sort`
- [ ] Uses projections for read-only queries to optimize performance

**Review Checklist:**
- ✓ Repository methods are simple and focused
- ✓ Custom queries documented (especially native SQL)
- ✓ Pagination used for large datasets
- ✓ No business logic in repository layer
- ✓ Queries are optimized (no SELECT *)

### 3.2 Entity Mapping & ORM
**Generation Criteria:**
- [ ] Entities properly annotated with `@Entity`, `@Table`
- [ ] Uses appropriate ID generation strategy (`@GeneratedValue`)
- [ ] Relationships properly defined (`@OneToOne`, `@OneToMany`, `@ManyToMany`)
- [ ] Lazy/Eager loading configured appropriately
- [ ] Uses Lombok for boilerplate reduction (`@Data`, `@Builder`)

**Review Checklist:**
- ✓ N+1 query problem avoided (proper fetch types)
- ✓ Cascade types appropriate for the relationship
- ✓ Entities don't expose sensitive data
- ✓ Bidirectional relationships properly managed
- ✓ Entity equals/hashCode appropriate

### 3.3 Transaction Management
**Generation Criteria:**
- [ ] Uses `@Transactional` at service layer (not controller)
- [ ] Specifies `readOnly=true` for query-only methods
- [ ] Proper propagation behavior configured where needed
- [ ] Handles transaction rollback scenarios
- [ ] Timeout configured for long-running transactions

**Review Checklist:**
- ✓ Transactions at appropriate layer (service, not controller/repository)
- ✓ Read-only transactions marked for optimization
- ✓ Rollback strategy understood and implemented
- ✓ No nested transactions creating issues
- ✓ Deadlock potential minimized

---

## 4. Service Layer & Business Logic

### 4.1 Service Design
**Generation Criteria:**
- [ ] Business logic encapsulated in `@Service` classes
- [ ] Services use repositories for data access (not entities directly)
- [ ] Methods follow single responsibility principle
- [ ] Complex logic extracted into separate methods
- [ ] DTOs used for request/response (not entities)

**Review Checklist:**
- ✓ Services only contain business logic
- ✓ No SQL/query logic in services
- ✓ No direct entity exposure to controllers
- ✓ Methods are testable and mockable
- ✓ Consistent naming conventions

### 4.2 Data Transfer Objects (DTOs)
**Generation Criteria:**
- [ ] Request DTOs for incoming data with validation annotations
- [ ] Response DTOs for outgoing data
- [ ] Entities never exposed directly to API clients
- [ ] DTO mapping logic separated (manual, MapStruct, or ModelMapper)
- [ ] DTOs include only necessary fields

**Review Checklist:**
- ✓ DTOs have proper validation annotations
- ✓ Mapping logic is reusable
- ✓ No sensitive data in response DTOs
- ✓ DTOs are immutable where appropriate
- ✓ Consistent DTO naming convention

---

## 5. Testing & Code Quality

### 5.1 Unit Testing
**Generation Criteria:**
- [ ] Uses JUnit 5 with proper test structure
- [ ] Mocks external dependencies with Mockito
- [ ] Follows Arrange-Act-Assert pattern
- [ ] Tests both success and failure scenarios
- [ ] Achieves minimum 70% code coverage for critical paths

**Review Checklist:**
- ✓ Tests are independent (no shared state)
- ✓ Test data setup is clear and maintainable
- ✓ Meaningful assertions (not just null checks)
- ✓ Test method names clearly describe what's tested
- ✓ No hardcoded values in tests

### 5.2 Integration Testing
**Generation Criteria:**
- [ ] Uses `@SpringBootTest` for full-context tests
- [ ] Tests real database interactions (H2, TestContainers)
- [ ] Uses `@Transactional` with rollback for test isolation
- [ ] Mocks external services (HTTP, messaging)
- [ ] Separate integration tests from unit tests

**Review Checklist:**
- ✓ Each test is independent (can run in any order)
- ✓ Test data cleaned up after each test
- ✓ Database transactions rolled back
- ✓ External API calls mocked appropriately
- ✓ Integration tests don't duplicate unit tests

---

## 6. Security Best Practices

### 6.1 Authentication & Authorization
**Generation Criteria:**
- [ ] Uses Spring Security framework appropriately
- [ ] Implements token-based auth (JWT) or session-based auth
- [ ] Role-based access control with `@PreAuthorize`
- [ ] Passwords hashed with BCrypt or Argon2
- [ ] CORS configured for frontend integration

**Review Checklist:**
- ✓ All sensitive endpoints require authentication
- ✓ Authorization checks prevent unauthorized access
- ✓ Passwords never logged or exposed
- ✓ Tokens have appropriate expiration
- ✓ CSRF protection enabled where applicable

### 6.2 Input Validation & Sanitization
**Generation Criteria:**
- [ ] All user input validated at controller level
- [ ] Uses `@Valid` with JSR-303/JSR-380 annotations
- [ ] Prevents SQL injection with parameterized queries
- [ ] No eval-like operations on user input
- [ ] File upload size and type validation

**Review Checklist:**
- ✓ Input validation catches invalid data early
- ✓ Error messages don't reveal system details
- ✓ All database queries parameterized
- ✓ No string concatenation in SQL
- ✓ File uploads validated and scanned

### 6.3 Sensitive Data Protection
**Generation Criteria:**
- [ ] No hardcoded secrets or credentials
- [ ] Uses environment variables for sensitive config
- [ ] Passwords and tokens never logged
- [ ] PII masked in logs where logged
- [ ] HTTPS enforced in production

**Review Checklist:**
- ✓ Secrets managed via environment variables or vaults
- ✓ Sensitive data not in version control
- ✓ Logs don't contain passwords, tokens, PII
- ✓ API keys rotated regularly
- ✓ HTTPS enforced for all endpoints

---

## 7. Logging & Monitoring

### 7.1 Logging Implementation
**Generation Criteria:**
- [ ] Uses SLF4J with Logback (or Log4j2)
- [ ] Appropriate log levels (DEBUG, INFO, WARN, ERROR)
- [ ] Logs business-critical events
- [ ] No sensitive data in logs
- [ ] Structured logging with key-value pairs

**Review Checklist:**
- ✓ Log levels used correctly
- ✓ No excessive logging (INFO not DEBUG in production)
- ✓ Error logs include context for debugging
- ✓ Consistent log format across application
- ✓ Correlation IDs for request tracing

### 7.2 Health Checks & Metrics
**Generation Criteria:**
- [ ] Spring Boot Actuator endpoints exposed appropriately
- [ ] Custom health checks for critical dependencies
- [ ] Metrics exposed for monitoring (Micrometer)
- [ ] Readiness and liveness probes configured
- [ ] Graceful shutdown implemented

**Review Checklist:**
- ✓ Health check covers database, cache, external APIs
- ✓ Metrics meaningful and actionable
- ✓ No sensitive information in actuator endpoints
- ✓ Monitoring alerts configured
- ✓ Graceful shutdown prevents data loss

---

## 8. Performance & Scalability

### 8.1 Database Optimization
**Generation Criteria:**
- [ ] Appropriate indexing on frequently queried columns
- [ ] N+1 query problem avoided (use fetch strategies)
- [ ] Query optimization with `@EntityGraph` or projections
- [ ] Connection pooling configured (HikariCP)
- [ ] Slow query logging enabled

**Review Checklist:**
- ✓ No SELECT * queries (specify needed columns)
- ✓ Large result sets paginated
- ✓ Indexes on foreign keys and WHERE clauses
- ✓ Query execution plans reviewed
- ✓ Connection pool tuned for workload

### 8.2 Caching Strategy
**Generation Criteria:**
- [ ] Caching implemented for frequently accessed data
- [ ] Appropriate cache backend (Redis, Ehcache)
- [ ] Cache invalidation strategy clear
- [ ] Cache key strategy prevents collisions
- [ ] Cache-aside or write-through pattern used

**Review Checklist:**
- ✓ Cache TTL appropriate for data freshness
- ✓ Cache hit rate monitored
- ✓ Cache stampede problem prevented
- ✓ Distributed cache consistency handled
- ✓ Fallback when cache unavailable

### 8.3 API Performance
**Generation Criteria:**
- [ ] Pagination implemented for list endpoints
- [ ] Response DTOs optimized (no unnecessary data)
- [ ] Compression enabled for large responses
- [ ] Connection timeouts configured
- [ ] Rate limiting implemented where appropriate

**Review Checklist:**
- ✓ Response times acceptable (<1s for UI endpoints)
- ✓ No N+1 queries in list endpoints
- ✓ Pagination prevents memory issues
- ✓ Timeouts prevent hanging requests
- ✓ Rate limiting prevents abuse

---

## 9. Code Quality & Standards

### 9.1 Code Organization & Style
**Generation Criteria:**
- [ ] Follows Java naming conventions (camelCase, PascalCase)
- [ ] Classes have single responsibility
- [ ] Methods are short and focused (<30 lines)
- [ ] Consistent code formatting (use Checkstyle/Spotless)
- [ ] No duplicate code (DRY principle)

**Review Checklist:**
- ✓ Package structure logical and organized
- ✓ Class names reflect responsibility
- ✓ Method names clearly describe function
- ✓ Consistent indentation and formatting
- ✓ No "god classes" with too many responsibilities

### 9.2 Documentation
**Generation Criteria:**
- [ ] Public API methods documented with JavaDoc
- [ ] Complex business logic has inline comments
- [ ] README includes setup and running instructions
- [ ] API endpoints documented (Swagger/OpenAPI)
- [ ] Configuration options documented

**Review Checklist:**
- ✓ JavaDoc includes @param, @return, @throws
- ✓ README up-to-date and complete
- ✓ API docs auto-generated from code
- ✓ Example requests/responses provided
- ✓ Deployment instructions clear

---

## 10. Build & Dependency Management

### 10.1 Maven/Gradle Configuration
**Generation Criteria:**
- [ ] Clean, organized `pom.xml` or `build.gradle`
- [ ] Dependency versions managed centrally
- [ ] Only necessary dependencies included
- [ ] Build plugins configured (compiler, surefire, etc.)
- [ ] Build profiles for different environments

**Review Checklist:**
- ✓ No version conflicts or diamond dependencies
- ✓ Transitive dependencies understood
- ✓ Security vulnerabilities scanned
- ✓ Build reproducible across machines
- ✓ No unused dependencies (bloat)

### 10.2 Deployment Readiness
**Generation Criteria:**
- [ ] Application starts without errors
- [ ] Graceful shutdown configured
- [ ] Health checks pass before accepting traffic
- [ ] Logging configured appropriately
- [ ] External service connectivity validated on startup

**Review Checklist:**
- ✓ All required environment variables checked
- ✓ Database migrations run automatically
- ✓ Cache warmed if necessary
- ✓ Error messages don't leak system info
- ✓ Rollback strategy documented

---

## 11. Version Control & Collaboration

### 11.1 Git Practices
**Generation Criteria:**
- [ ] Clear, descriptive commit messages
- [ ] Commits are atomic and logical
- [ ] `.gitignore` excludes sensitive files and artifacts
- [ ] Feature branches for new work
- [ ] Pull requests for code review

**Review Checklist:**
- ✓ Commit history is readable
- ✓ No sensitive data committed
- ✓ Branch naming follows convention
- ✓ PR description explains changes
- ✓ No merge commits cluttering history

### 11.2 Code Review Standards
**Generation Criteria:**
- [ ] All changes reviewed before merge
- [ ] Tests added for new features
- [ ] Documentation updated with changes
- [ ] No technical debt introduced
- [ ] Follows team coding standards

**Review Checklist:**
- ✓ Reviewer understands the change
- ✓ Tests verify the fix/feature
- ✓ No breaking changes without migration plan
- ✓ Performance impact considered
- ✓ Security implications reviewed

---

## Scoring Rubric

### Production Code Quality (0-100)
| Score | Rating | Description |
|-------|--------|-------------|
| 90-100 | ⭐⭐⭐⭐⭐ Excellent | Production-ready, follows all standards, best practices applied |
| 80-89 | ⭐⭐⭐⭐ Good | Mostly production-ready, minor issues, most standards followed |
| 70-79 | ⭐⭐⭐ Acceptable | Functional but needs improvements, several standards missing |
| 60-69 | ⭐⭐ Poor | Significant issues, many standards violated |
| <60 | ⭐ Critical | Not production-ready, major issues present |

### Code Review Quality (0-100)
| Score | Rating | Description |
|-------|--------|-------------|
| 90-100 | ⭐⭐⭐⭐⭐ Excellent | Catches all critical issues, provides actionable feedback with solutions |
| 80-89 | ⭐⭐⭐⭐ Good | Catches most critical issues, helpful feedback |
| 70-79 | ⭐⭐⭐ Acceptable | Catches some issues, feedback could be more complete |
| 60-69 | ⭐⭐ Poor | Misses significant issues, limited feedback |
| <60 | ⭐ Critical | Ineffective review, misses major problems |

---

## Critical Issues (Must-Fix)

The following are **non-negotiable** and must be addressed before code can be merged:

- ❌ Hardcoded credentials or secrets
- ❌ SQL injection vulnerabilities
- ❌ Missing authentication on sensitive endpoints
- ❌ Unhandled exceptions reaching clients
- ❌ No tests for critical paths
- ❌ PII or sensitive data exposed in logs/responses
- ❌ Deprecated or vulnerable dependencies

---

## Quality Metrics

Track these metrics to maintain code health:

| Metric | Target | Tool |
|--------|--------|------|
| Code Coverage | >70% | JaCoCo |
| Bug Density | <1 per 1000 LOC | SonarQube |
| Test Pass Rate | 100% | CI/CD Pipeline |
| Security Vulnerabilities | 0 Critical, 0 High | OWASP, Snyk |
| Technical Debt Ratio | <5% | SonarQube |
| Cyclomatic Complexity | <10 per method | SonarQube |
| Code Duplication | <3% | SonarQube |
| Response Time (p99) | <1s | APM |

---

## Quick Reference Checklists

### Before Code Review
- [ ] Code compiles without warnings
- [ ] All tests pass locally
- [ ] No hardcoded values
- [ ] No debug logging left in code
- [ ] JavaDoc added for public APIs
- [ ] Commit message is descriptive

### Before Merging to Main
- [ ] Code review approved
- [ ] All CI/CD checks pass
- [ ] Test coverage maintained
- [ ] No security issues flagged
- [ ] Performance impact assessed
- [ ] Release notes updated if needed

### Before Production Deployment
- [ ] Staging environment tests pass
- [ ] Database migrations tested
- [ ] Rollback plan documented
- [ ] Monitoring and alerts configured
- [ ] Load testing completed (if needed)
- [ ] Security scan passed

---

## Learning Resources

- **Spring Boot Official Docs**: https://spring.io/projects/spring-boot
- **Spring Framework Best Practices**: https://spring.io/guides
- **Java Concurrency**: Effective Java by Joshua Bloch
- **System Design**: Designing Data-Intensive Applications
- **Security**: OWASP Top 10, Spring Security Guide

---

**Version**: 1.0 | **Last Updated**: 2026 | **Status**: Ready for Production Use

