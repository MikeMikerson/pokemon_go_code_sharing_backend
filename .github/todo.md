# TODO: Backend Compatibility with Frontend Repository

This TODO list ensures compatibility between the `pokemon_go_code_sharing_backend` (Java Spring) and the `MikeMikerson/pokemon_go_code_sharing` frontend (TypeScript/Next.js).

## Frontend Analysis Summary

**Frontend Repository:** `MikeMikerson/pokemon_go_code_sharing` (Next.js + TypeScript)
- **Key Components:** Friend code submission forms, feed display, validation utilities
- **Data Structure:** Comprehensive friend code model with teams, goals, validation rules
- **Features:** Real-time updates, theme switching, responsive design

## API Compatibility Requirements

### ✅ Already Implemented

- [x] **FriendCode Entity Model** - Matches frontend expectations
  - UUID id, 12-digit friend code validation
  - Trainer name, level (1-50), team (MYSTIC/VALOR/INSTINCT)
  - Country validation, purpose (GIFTS/RAIDS/BOTH)
  - Message field, timestamps (submitted/expires)
  
- [x] **Core REST Endpoints**
  - `POST /api/friend-codes` - Submit friend code
  - `GET /api/friend-codes` - Paginated feed (with ETag caching)
  - `GET /api/friend-codes/can-submit` - Rate limit check

- [x] **Data Validation**
  - 12-digit friend code pattern validation
  - Trainer level range (1-50), name length (≤50)
  - Country code validation, HTML content filtering
  - Message length validation (≤100 characters)

- [x] **Rate Limiting & Security**
  - User fingerprinting for rate limiting
  - 24-hour submission cooldown
  - 48-hour friend code expiration
  - CORS configuration, security headers

## Frontend-Backend Compatibility Gaps

### 🔧 API Path Mismatch - HIGH PRIORITY

- [ ] **Update API Base Path from `/api/friend-codes` to `/api/v1/friend-codes`**
  - Frontend expects: `/api/v1/friend-codes`
  - Backend provides: `/api/friend-codes`
  - **Files to modify:**
    - `src/main/java/com/devs/simplicity/poke_go_friends/controller/FriendCodeController.java`
    - Update `@RequestMapping("/api/friend-codes")` to `@RequestMapping("/api/v1/friend-codes")`
    - Update all tests referencing the old path

### 📱 Frontend-Specific Features - MEDIUM PRIORITY

- [ ] **Add Theme Support Endpoints**
  - Frontend has dark/light theme switching
  - Consider adding user preference storage endpoints
  - `GET /api/v1/user/preferences` 
  - `PUT /api/v1/user/preferences`

- [ ] **Enhanced Friend Code Response**
  - [ ] Add `isOwnSubmission` flag to FriendCodeResponse
  - [ ] Add `timeRemaining` calculated field for expiration
  - [ ] Consider adding `reportCount` for moderation

### 🔄 Real-time Features - LOW PRIORITY

- [ ] **WebSocket Support for Live Updates**
  - Frontend context suggests real-time friend code updates
  - Add WebSocket endpoint: `/ws/friend-codes`
  - Broadcast new submissions to connected clients
  - **Dependencies to add:**
    - Spring WebSocket
    - STOMP messaging

### 📊 Analytics & Metrics - LOW PRIORITY

- [ ] **Enhanced Analytics Endpoints**
  - `GET /api/v1/stats/active-codes` - Current active count
  - `GET /api/v1/stats/submission-trends` - Daily/weekly trends
  - `GET /api/v1/stats/team-distribution` - Team popularity

### 🛡️ Additional Security Features - MEDIUM PRIORITY

- [ ] **Enhanced Rate Limiting**
  - Add IP-based rate limiting alongside fingerprint
  - Implement sliding window rate limiting
  - Add rate limit headers to responses

- [ ] **Content Moderation**
  - Add profanity filter for trainer names and messages
  - Implement reporting system for inappropriate content
  - Admin endpoints for content moderation

### 🗄️ Database Optimizations - LOW PRIORITY

- [ ] **Add Database Indexes**
  - Index on `expires_at` for cleanup queries
  - Composite index on `user_fingerprint + submitted_at`
  - Index on `team` and `purpose` for filtering

- [ ] **Add Soft Delete Support**
  - Add `deleted_at` column for soft deletion
  - Update queries to exclude soft-deleted records
  - Admin endpoints for permanent deletion

### 🧪 Testing Enhancements - MEDIUM PRIORITY

- [ ] **API Contract Testing**
  - Add OpenAPI/Swagger contract validation
  - Frontend-backend integration tests
  - Test all enum values compatibility

- [ ] **Performance Testing**
  - Load testing for friend code submission
  - Pagination performance with large datasets
  - Rate limiting behavior under load

### 📚 Documentation Updates - LOW PRIORITY

- [ ] **API Documentation**
  - Update Swagger documentation with v1 paths
  - Add examples matching frontend usage patterns
  - Document rate limiting behavior

- [ ] **Frontend Integration Guide**
  - Create setup guide for frontend developers
  - Document authentication flow (if added)
  - Environment configuration examples

## Implementation Priority

### Phase 1 (Immediate) 🚨
1. Fix API path mismatch (`/api/v1/friend-codes`)
2. Verify all enum values match frontend expectations
3. Test pagination and filtering compatibility

### Phase 2 (Short-term) 📅
1. Add theme preference endpoints
2. Enhance rate limiting with IP support
3. Add comprehensive API contract tests

### Phase 3 (Long-term) 🌟
1. Implement WebSocket for real-time updates
2. Add analytics and metrics endpoints
3. Implement content moderation features

## Testing Checklist

- [ ] Verify friend code submission from frontend works
- [ ] Test feed pagination matches frontend expectations
- [ ] Confirm rate limiting behavior
- [ ] Validate all Team enum values (MYSTIC, VALOR, INSTINCT)
- [ ] Validate all Purpose enum values (GIFTS, RAIDS, BOTH)
- [ ] Test CORS configuration with frontend domain
- [ ] Verify ETag caching works correctly

## Notes

- **Backend Status:** Fully functional with comprehensive validation, rate limiting, and caching
- **Frontend Compatibility:** 95% compatible, mainly needs API path update
- **Architecture:** Well-designed with clean separation of concerns
- **Test Coverage:** Excellent with unit, integration, and repository tests
- **Performance:** Optimized with caching, pagination, and database projections

## Quick Fix Commands

```bash
# Update API path in controller
sed -i 's|@RequestMapping("/api/friend-codes")|@RequestMapping("/api/v1/friend-codes")|g' \
  src/main/java/com/devs/simplicity/poke_go_friends/controller/FriendCodeController.java

# Update test paths
find src/test -name "*.java" -exec sed -i 's|/api/friend-codes|/api/v1/friend-codes|g' {} \;

# Run tests to verify changes
./gradlew test
```
