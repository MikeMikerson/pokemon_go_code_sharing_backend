# Backend Sync Requirements Todo List

Based on analysis of the frontend (`simple_pokemon_go_code_sharing`) and backend (`poke-go-friends`), here are the tasks needed to sync the backend with frontend expectations:

## API Structure & CORS
- [ ] **Add CORS configuration** - Frontend expects to call backend from different origin (localhost:3000 to localhost:8080)
- [ ] **Verify API base path** - Frontend expects `/api/*` endpoints, backend provides `/api/*` ✅
- [ ] **Add API versioning support** - Frontend uses `/api/` but no version prefix currently

## Missing Endpoints
- [x] **Add actuator health endpoint mapping** - Frontend calls `/api/health` but backend has custom health controller, need to verify compatibility ✅ (Both endpoints working)
- [x] **Verify health response format** - Frontend expects `{status: 'UP'|'DOWN', timestamp: string}` format ✅ (Confirmed working)

## Data Structure Gaps
- [ ] **Add team field support** - Frontend sends Pokemon Go team (`mystic`|`valor`|`instinct`) but backend doesn't store it
- [ ] **Add goals field support** - Frontend sends goals array (`gifts`|`exp`|`raids`|`all`) but backend doesn't store it
- [ ] **Update FriendCode entity** - Add team and goals fields to database schema
- [ ] **Update DTOs** - Add team and goals to FriendCodeSubmissionRequest and FriendCodeResponse
- [ ] **Create database migration** - Add team and goals columns to friend_codes table

## Validation Alignment
 - [x] **Sync friend code validation** - Frontend expects exactly 12 digits, backend has same validation ✅
 - [x] **Sync trainer level validation** - Frontend expects 1-50, backend has same validation ✅
 - [x] **Add team validation** - Backend needs to validate team enum values (currently, invalid values cause deserialization errors, not user-friendly validation)
 - [x] **Add goals validation** - Backend needs to validate goals array values (currently, invalid values cause deserialization errors, not user-friendly validation)
 - [x] **Update trainer name validation** - Frontend allows optional trainer name, backend requires it (now optional in backend, matches frontend) ✅

## Response Format Adjustments
- [ ] **Update error response format** - Ensure ValidationError responses include fieldErrors map
- [ ] **Add proper HTTP status codes** - Verify rate limiting returns 429 with Retry-After header
- [ ] **Update pagination response** - Verify FriendCodeFeedResponse matches frontend expectations

## Frontend-Backend Mapping Issues
- [ ] **Fix trainer name mapping** - Frontend uses `trainerName` field, backend expects required field
- [ ] **Fix player level mapping** - Frontend sends `trainerLevel` as string, backend expects `playerLevel` as integer
- [ ] **Add default expiration logic** - Frontend expects `expiresAt` field, backend should set default 24-48 hour expiration

## Rate Limiting & Security
- [ ] **Implement proper rate limiting** - Frontend expects rate limit errors with retry information
- [ ] **Add IP-based rate limiting** - For anonymous submissions
- [ ] **Add proper error handling** - For rate limit exceeded scenarios

## Database & Configuration
- [x] **Add PostgreSQL setup documentation** - For local development ✅ (Docker Compose setup working)
- [ ] **Add Flyway migration for new fields** - team and goals columns
- [x] **Update application properties** - For any new configuration needed ✅ (Database connection configured)

## Testing & Validation
- [ ] **Add integration tests** - For new team and goals functionality
- [ ] **Test CORS configuration** - Ensure frontend can communicate with backend
- [ ] **Test all API endpoints** - Verify frontend compatibility
- [ ] **Test error handling** - Ensure proper error responses

## Documentation Updates
- [ ] **Update API documentation** - Include new team and goals fields
- [ ] **Update README** - With setup instructions for both frontend and backend
- [ ] **Add environment configuration guide** - For connecting frontend to backend

## Priority Order
1. **High Priority**: CORS configuration, team/goals fields, validation alignment
2. **Medium Priority**: Error response format, rate limiting, database migration
3. **Low Priority**: Documentation updates, testing improvements

## Notes
- The backend has good foundational structure with proper controllers, services, and DTOs
- Most core functionality exists, mainly missing Pokemon Go specific fields (team, goals)
- Rate limiting is configured but may need adjustment for frontend integration
- Health endpoints exist but may need format verification
- ✅ **Database setup completed** - PostgreSQL connection working, all migrations applied successfully
- ✅ **Schema validation fixed** - Missing user_id column added, relationships working properly
- ✅ **Docker Compose configuration verified** - Backend and database containers communicating correctly