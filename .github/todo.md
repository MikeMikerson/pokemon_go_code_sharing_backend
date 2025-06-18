# Backend TODO List: Pokémon Go Friend Code Sharing API (Spring Fram## 4. Core Business Logic Services
- [x] Create `FriendCodeService`:
    - [x] Submit friend code logic
    - [x] Validate all input fields
    - [x] Generate expiry timestamp (24-48 hours)
    - [x] Check for expired codes before returning
- [x] Create `RateLimitService`:
    - [x] Generate user fingerprint from request (IP + User-Agent hash)
    - [x] Check if user can submit (24-hour cooldown)
    - [x] Store submission timestamps in Redis
    - [x] Calculate next allowed submission time
- [x] Create `FingerprintService`:
    - [x] Generate consistent fingerprint from request
    - [x] Hash IP address for privacy
    - [x] Include User-Agent in fingerprint
- [ ] Create `CleanupService`:
    - [ ] Scheduled task to delete expired codes
    - [ ] Run every hour or configurable intervalect Setup & Configuration
- [x] Initialize Spring Boot project with dependencies:
    - [x] Spring Web
    - [x] Spring Data JPA
    - [x] PostgreSQL Driver
    - [x] Spring Validation
    - [x] Spring Security (for rate limiting)
    - [x] Lombok
    - [x] SpringDoc OpenAPI (Swagger)
    - [x] Spring Data Redis
    - [x] Testcontainers (for testing)
    - [x] Logstash encoder (for structured logging)
- [x] Configure `application.properties` / `application.yml`:
    - [x] Database connection settings
    - [x] Server port configuration
    - [x] CORS settings for Next.js frontend
    - [x] Redis cache configuration
    - [x] Rate limiting parameters
- [x] Set up development and production profiles
- [x] Configure logging (Logback)

---

## 2. Database Schema & Entities
- [x] Create database migration scripts (Flyway or Liquibase):
    - [x] `friend_codes` table with columns:
        - `id` (UUID, primary key)
        - `friend_code` (VARCHAR(12), not null)
        - `trainer_name` (VARCHAR(50), nullable)
        - `trainer_level` (INTEGER, nullable, 1-50)
        - `team` (VARCHAR(20), nullable, enum: Instinct/Mystic/Valor)
        - `country` (VARCHAR(50), nullable)
        - `purpose` (VARCHAR(20), nullable, enum: Gifts/Raids/Both)
        - `message` (VARCHAR(100), nullable)
        - `submitted_at` (TIMESTAMP, not null)
        - `expires_at` (TIMESTAMP, not null)
        - `user_fingerprint` (VARCHAR(255), not null, for rate limiting)
    - [x] Index on `expires_at` for cleanup queries
    - [x] Index on `submitted_at` for sorting
    - [x] Index on `user_fingerprint` for rate limit checks
- [x] Create JPA Entity classes:
    - [x] `FriendCode` entity with validation annotations
    - [x] Enum classes for `Team` and `Purpose`
- [x] Create Spring Data JPA repositories

---

## 3. API Models & DTOs
- [x] Create request/response DTOs matching frontend types:
    - [x] `FriendCodeSubmissionRequest` DTO:
        - `friendCode` (12 digits, validated)
        - `trainerName` (optional, max 50 chars)
        - `trainerLevel` (optional, 1-50)
        - `team` (optional, enum validation)
        - `country` (optional, predefined list)
        - `purpose` (optional, enum validation)
        - `message` (optional, max 100 chars)
    - [x] `FriendCodeResponse` DTO
    - [x] `SubmissionResponse` DTO (success, message, nextSubmissionAllowed)
    - [x] `FriendCodeFeedResponse` DTO (list, hasMore, nextCursor)
    - [x] `ErrorResponse` DTO for standardized errors
- [x] Create mapper classes (MapStruct or manual)
- [x] Add Jakarta Validation annotations

---

## 4. Core Business Logic Services
- [x] Create `FriendCodeService`:
    - [x] Submit friend code logic
    - [x] Validate all input fields
    - [x] Generate expiry timestamp (24-48 hours)
    - [x] Check for expired codes before returning
- [x] Create `RateLimitService`:
    - [x] Generate user fingerprint from request (IP + User-Agent hash)
    - [x] Check if user can submit (24-hour cooldown)
    - [x] Store submission timestamps in Redis
    - [x] Calculate next allowed submission time
- [x] Create `FingerprintService`:
    - [x] Generate consistent fingerprint from request
    - [x] Hash IP address for privacy
    - [x] Include User-Agent in fingerprint
- [x] Create `CleanupService`:
    - [x] Scheduled task to delete expired codes
    - [x] Run every hour or configurable interval

---

## 5. REST API Controllers
- [x] Create `FriendCodeController`:
    - [x] `POST /api/friend-codes` - Submit new friend code
        - [x] Validate request body
        - [x] Check rate limit
        - [x] Save to database
        - [x] Return success/error response
    - [x] `GET /api/friend-codes` - Get friend codes feed
        - [x] Support pagination parameters (page, size)
        - [x] Sort by newest first
        - [x] Filter out expired codes
        - [x] Return paginated response
    - [x] `GET /api/friend-codes/can-submit` - Check if user can submit
        - [x] Return boolean and next submission time
- [x] Add proper HTTP status codes:
    - [x] 201 Created for successful submission
    - [x] 429 Too Many Requests for rate limit
    - [x] 400 Bad Request for validation errors
- [x] Configure CORS for Next.js frontend URL

---

## 6. Rate Limiting Implementation
- [x] Configure Redis for distributed rate limiting:
    - [x] Store user fingerprints with TTL
    - [x] Use Redis SET with NX and EX options
    - [x] Implement Lua scripts for atomic operations
    - [x] Support both fixed window and sliding window algorithms
- [x] Create custom `@RateLimited` annotation
- [x] Implement AOP aspect for rate limiting
- [x] Return proper error response with retry-after header
- [x] Consider implementing sliding window algorithm
- [x] Add distributed lock support for critical operations
- [x] Enhanced Redis configuration with Lua scripts
- [x] Comprehensive test coverage for new components

---

## 7. Security & Validation
- [ ] Input validation for all fields:
    - [ ] Friend code: exactly 12 digits
    - [ ] Trainer level: 1-50 range
    - [ ] Team: valid enum value
    - [ ] Country: predefined list validation
    - [ ] Purpose: valid enum value
    - [ ] Message: max 100 characters, sanitize HTML
- [ ] Implement request sanitization
- [ ] Add request size limits
- [ ] Configure security headers
- [ ] Implement CORS properly (no wildcards in production)
- [ ] Add API versioning strategy

---

## 8. Error Handling & Logging
- [x] Create global exception handler (`@ControllerAdvice`):
    - [x] Handle validation errors
    - [x] Handle rate limit exceeded
    - [x] Handle database errors
    - [x] Return consistent error format
- [ ] Add request/response logging interceptor
- [ ] Log rate limit violations
- [ ] Add correlation IDs for request tracking
- [ ] Configure different log levels per environment

---

## 9. Testing
- [x] Unit tests for all service methods:
    - [x] Test friend code submission logic
    - [x] Test rate limiting logic
    - [x] Test validation rules
    - [x] Test fingerprint generation
- [x] Integration tests for REST endpoints:
    - [x] Test successful submission
    - [x] Test rate limit enforcement
    - [x] Test validation errors
    - [x] Test pagination
- [x] Repository layer tests with @DataJpaTest
- [x] Mock Redis for rate limit tests
- [x] Add test fixtures and builders

---

## 10. Performance & Optimization
- [ ] Add database connection pooling (HikariCP)
- [ ] Configure JPA query optimization:
    - [ ] Use projections for read-only queries
    - [ ] Add appropriate fetch strategies
- [ ] Implement response caching:
    - [ ] Cache friend codes feed (1-5 minutes)
    - [ ] Use ETags for conditional requests
- [ ] Add pagination limits (max 100 per page)
- [ ] Monitor slow queries

---

## 11. Monitoring & Health Checks
- [ ] Add Spring Boot Actuator:
    - [ ] Health endpoint
    - [ ] Metrics endpoint
    - [ ] Custom health indicator for Redis
- [ ] Add custom metrics:
    - [ ] Submission count
    - [ ] Rate limit hit count
    - [ ] Active friend codes count
- [ ] Configure structured logging (JSON format)
- [ ] Add database migration status check

---

## 12. Documentation & API Specs
- [x] Configure SpringDoc OpenAPI:
    - [x] Document all endpoints
    - [x] Add example requests/responses
    - [x] Document error codes
- [ ] Create API versioning strategy
- [ ] Add README with setup instructions
- [ ] Document rate limiting behavior
- [ ] Create Postman collection

---

## 13. Deployment Preparation
- [ ] Create Dockerfile for Spring Boot app
- [ ] Add health check endpoint for container orchestration
- [ ] Configure environment-specific properties
- [ ] Set up database migration on startup
- [ ] Add graceful shutdown configuration
- [ ] Configure JVM memory settings
- [ ] Add build info endpoint

---

## 14. Future Considerations (Post-MVP)
- [ ] Implement CAPTCHA integration endpoint
- [ ] Add admin endpoints for moderation
- [ ] Implement soft deletes for abuse tracking
- [ ] Add metrics dashboard
- [ ] Consider GraphQL alternative
- [ ] Add WebSocket for real-time updates
- [ ] Implement more sophisticated anti-spam measures

---

**Note:** Start with basic CRUD operations and rate limiting, then add security and optimization layers progressively.