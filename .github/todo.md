# Pokemon Go Code Sharing Backend - TODO

## Project Overview
This backend provides API services for a Pokemon Go friend code sharing application. The frontend allows users to submit, view, and share friend codes with other Pokemon Go players.

## Core Features to Implement

### 1. Data Models & Entities
- [x] **FriendCode Entity**
  - [x] Create `FriendCode` JPA entity with fields:
    - `id` (Long, Primary Key)
    - `friendCode` (String, 12-digit code with validation)
    - `trainerName` (String, Pokemon Go trainer name)
    - `playerLevel` (Integer, optional)
    - `location` (String, optional - city/country)
    - `description` (String, optional - what they're looking for)
    - `isActive` (Boolean, default true)
    - `createdAt` (LocalDateTime)
    - `updatedAt` (LocalDateTime)
    - `expiresAt` (LocalDateTime, optional)

- [x] **User Entity** (if authentication is needed)
  - [x] Create `User` JPA entity for authentication
  - [x] Link FriendCode submissions to users
  - [x] Include basic profile information

### 2. Repository Layer
- [x] **FriendCodeRepository**
  - [x] Create JPA repository interface
  - [x] Custom queries for:
    - Finding active friend codes
    - Searching by location
    - Finding recent submissions
    - Pagination support

### 3. Service Layer  
- [x] **FriendCodeService**
  - [x] Create friend code
  - [x] Validate friend code format (12 digits)
  - [x] Get paginated list of friend codes
  - [x] Filter by location, level range
  - [x] Search functionality
  - [x] Mark friend code as inactive/expired
  - [x] Duplicate detection logic

- [x] **ValidationService**
  - [x] Friend code format validation
  - [x] Rate limiting per IP/user
  - [x] Content moderation (inappropriate names/descriptions)

### 4. Controller Layer
- [ ] **FriendCodeController** REST API endpoints:
  - [ ] `POST /api/friend-codes` - Submit new friend code
  - [ ] `GET /api/friend-codes` - Get paginated list with filters
  - [ ] `GET /api/friend-codes/{id}` - Get specific friend code
  - [ ] `PUT /api/friend-codes/{id}` - Update friend code (if owned)
  - [ ] `DELETE /api/friend-codes/{id}` - Deactivate friend code
  - [ ] `GET /api/friend-codes/search` - Search with query parameters

- [ ] **HealthController** 
  - [ ] Basic health check endpoint
  - [ ] Database connectivity check

### 5. DTOs (Data Transfer Objects)
- [ ] **FriendCodeSubmissionRequest**
  - [ ] Validation annotations
  - [ ] Required fields: friendCode, trainerName
  - [ ] Optional fields: playerLevel, location, description

- [ ] **FriendCodeResponse**
  - [ ] Public response format
  - [ ] Include all safe fields to display

- [ ] **FriendCodeFeedResponse** 
  - [ ] Paginated response wrapper
  - [ ] Include metadata (total count, page info)

- [ ] **ErrorResponse**
  - [ ] Standardized error format
  - [ ] Include error codes and messages

### 6. Database Configuration
- [ ] **Database Setup**
  - [ ] Configure PostgreSQL connection
  - [ ] Set up connection pooling
  - [ ] Configure for different environments (dev, prod)

- [ ] **Flyway Migrations**
  - [ ] Create initial schema migration
  - [ ] Add indexes for common queries
  - [ ] Set up versioning strategy

### 7. Security & Validation
- [ ] **Input Validation**
  - [ ] Friend code format validation (exactly 12 digits)
  - [ ] Trainer name length and character validation
  - [ ] Sanitize all text inputs
  - [ ] Rate limiting configuration

- [ ] **CORS Configuration**
  - [ ] Configure CORS for frontend domain
  - [ ] Set appropriate headers
  - [ ] Handle preflight requests

- [ ] **Security Headers**
  - [ ] Configure security headers
  - [ ] CSRF protection if needed
  - [ ] API rate limiting

### 8. Error Handling & Logging
- [ ] **Global Exception Handler**
  - [ ] Handle validation errors
  - [ ] Database constraint violations
  - [ ] Custom business logic exceptions
  - [ ] Return consistent error responses

- [ ] **Logging Configuration**
  - [ ] Structured logging with appropriate levels
  - [ ] Log API requests/responses
  - [ ] Security event logging
  - [ ] Performance monitoring

### 9. Configuration & Properties
- [ ] **Application Properties**
  - [ ] Database configuration
  - [ ] API configuration (base URL, timeouts)
  - [ ] Feature flags
  - [ ] Environment-specific settings

- [ ] **Profiles**
  - [ ] Development profile
  - [ ] Production profile  
  - [ ] Test profile with H2 database

### 10. Testing
- [ ] **Unit Tests**
  - [ ] Service layer tests
  - [ ] Repository tests with @DataJpaTest
  - [ ] Validation logic tests
  - [ ] Mock external dependencies

- [ ] **Integration Tests**
  - [ ] Controller integration tests with @SpringBootTest
  - [ ] Database integration tests
  - [ ] API endpoint tests
  - [ ] Test containers for PostgreSQL

- [ ] **Test Data**
  - [ ] Test fixtures and builders
  - [ ] Sample data for development
  - [ ] Database seeding scripts

### 11. API Documentation
- [ ] **OpenAPI/Swagger**
  - [ ] Set up Swagger UI
  - [ ] Document all endpoints
  - [ ] Include request/response examples
  - [ ] API versioning strategy

### 12. Performance & Monitoring
- [ ] **Caching**
  - [ ] Cache popular searches
  - [ ] Redis configuration if needed
  - [ ] Cache invalidation strategy

- [ ] **Monitoring**
  - [ ] Actuator endpoints
  - [ ] Prometheus metrics
  - [ ] Health checks
  - [ ] Performance monitoring

### 13. DevOps & Deployment
- [ ] **Docker**
  - [ ] Create Dockerfile
  - [ ] Docker Compose for local development
  - [ ] Multi-stage builds

- [ ] **CI/CD**
  - [ ] GitHub Actions workflow
  - [ ] Automated testing
  - [ ] Code quality checks
  - [ ] Deployment automation

### 14. Additional Features (Future)
- [ ] **Advanced Features**
  - [ ] Friend code QR code generation
  - [ ] Geolocation-based matching
  - [ ] Friend code expiration
  - [ ] Report/moderation system
  - [ ] Statistics and analytics
  - [ ] Email notifications
  - [ ] Social media integration

- [ ] **Admin Features**
  - [ ] Admin dashboard API
  - [ ] Content moderation tools
  - [ ] User management
  - [ ] Analytics endpoints

### 15. API Endpoints Summary
```
POST   /api/friend-codes              - Submit new friend code
GET    /api/friend-codes              - Get friend codes (paginated, filtered)
GET    /api/friend-codes/{id}         - Get specific friend code
PUT    /api/friend-codes/{id}         - Update friend code
DELETE /api/friend-codes/{id}         - Deactivate friend code
GET    /api/friend-codes/search       - Search friend codes
GET    /api/health                    - Health check
GET    /actuator/health               - Spring actuator health
```

### 16. Database Schema
```sql
CREATE TABLE friend_codes (
    id BIGSERIAL PRIMARY KEY,
    friend_code VARCHAR(12) NOT NULL UNIQUE,
    trainer_name VARCHAR(100) NOT NULL,
    player_level INTEGER,
    location VARCHAR(200),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_friend_codes_active ON friend_codes(is_active);
CREATE INDEX idx_friend_codes_created ON friend_codes(created_at);
CREATE INDEX idx_friend_codes_location ON friend_codes(location);
```

## Getting Started
1. Set up PostgreSQL database
2. Configure application properties
3. Run Flyway migrations
4. Implement core entities and repositories
5. Build service layer with validation
6. Create REST controllers
7. Add comprehensive testing
8. Set up monitoring and logging

## Notes
- Follow TDD approach - write tests first
- Use Spring Boot best practices
- Implement proper error handling
- Consider rate limiting from the start
- Plan for horizontal scaling
- Keep security in mind for all endpoints
- Document all APIs thoroughly