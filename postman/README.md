# Pokemon Go Friends API - Comprehensive Test Suite

This directory contains a comprehensive Postman test suite for the Pokemon Go Friend Code Sharing API. The test suite provides exhaustive coverage of all endpoints, edge cases, error scenarios, and performance validation.

## 📁 Files

- `poke-go-friends-comprehensive-test-suite.postman_collection.json` - Main comprehensive test collection
- `poke-go-friends-comprehensive-test.postman_environment.json` - Environment variables for testing
- `poke-go-friends-api.postman_collection.json` - Original basic collection (legacy)
- `poke-go-friends-local.postman_environment.json` - Local development environment

## 🧪 Test Categories

### 🏥 Health Endpoints
- **Health Check** - Basic application health monitoring
- **Readiness Check** - Kubernetes readiness probe validation
- **Liveness Check** - Kubernetes liveness probe validation

### ➕ Friend Code Creation
- **Valid Complete Data** - Full friend code submission with all fields
- **Minimal Valid Data** - Friend code with only required fields
- **Different Formats** - Testing various friend code formats (spaces, dashes)

### ❌ Validation Error Tests
- **Missing Required Fields** - Testing missing friendCode
- **Invalid Formats** - Various invalid friend code formats
- **Boundary Testing** - Player levels, trainer name lengths
- **Invalid Enums** - Testing invalid team/goal values
- **Duplicate Prevention** - Testing duplicate friend code handling
- **JSON Validation** - Malformed JSON testing

### 📖 Friend Code Retrieval
- **Default Parameters** - Basic pagination testing
- **Custom Pagination** - Various page sizes and numbers
- **Sorting** - Testing sort functionality
- **Filtering** - Location, level range, search filters
- **Parameter Validation** - Invalid parameter handling

### 🔍 Individual Friend Code Operations
- **GET by ID** - Retrieve specific friend codes
- **PUT Updates** - Full and partial updates
- **DELETE Operations** - Deactivation/deletion testing
- **Error Scenarios** - Not found, invalid data handling

### 🔎 Search Functionality
- **Trainer Name Search** - Searching by trainer name
- **Description Search** - Content-based searching
- **Multi-criteria Search** - Combined search parameters
- **Empty Results** - No matches scenario

### ⏰ Recent Friend Codes
- **Default Time Range** - Last 24 hours
- **Custom Time Range** - Configurable time windows
- **Pagination Support** - Paged recent results

### 📊 Statistics Endpoint
- **Basic Statistics** - System metrics retrieval
- **Data Validation** - Ensuring numeric consistency
- **Response Structure** - Validating statistics format

### 🚀 Performance & Load Tests
- **Large Page Sizes** - Maximum allowed page size testing
- **Response Time Validation** - Performance benchmarking
- **Sequential Requests** - Rapid request handling

### 🔐 Security & Edge Cases
- **SQL Injection Prevention** - Security vulnerability testing
- **XSS Protection** - Cross-site scripting prevention
- **Content-Type Validation** - Invalid media type handling
- **Unicode Support** - International character handling
- **Oversized Requests** - Large payload handling

## 🚀 Running the Tests

### Prerequisites
1. **Postman Desktop App** or **Newman CLI**
2. **Running API Server** (local or remote)
3. **Database Access** (for full functionality)

### Option 1: Postman Desktop App

1. **Import Collection and Environment:**
   ```
   File → Import → Select Files
   - Import: poke-go-friends-comprehensive-test-suite.postman_collection.json
   - Import: poke-go-friends-comprehensive-test.postman_environment.json
   ```

2. **Select Environment:**
   - Choose "Pokemon Go Friends - Comprehensive Test Environment" from dropdown

3. **Configure Base URL:**
   - Update `baseUrl` variable if not using `http://localhost:8080`

4. **Run Collection:**
   - Click "..." next to collection name
   - Select "Run collection"
   - Configure run settings as needed
   - Click "Run Pokemon Go Friends API - Comprehensive Test Suite"

### Option 2: Newman CLI

1. **Install Newman:**
   ```bash
   npm install -g newman
   ```

2. **Run Complete Test Suite:**
   ```bash
   newman run poke-go-friends-comprehensive-test-suite.postman_collection.json \
     -e poke-go-friends-comprehensive-test.postman_environment.json \
     --reporters cli,html \
     --reporter-html-export test-results.html
   ```

3. **Run Specific Folder:**
   ```bash
   newman run poke-go-friends-comprehensive-test-suite.postman_collection.json \
     -e poke-go-friends-comprehensive-test.postman_environment.json \
     --folder "Health Endpoints"
   ```

4. **Run with Custom Environment:**
   ```bash
   newman run poke-go-friends-comprehensive-test-suite.postman_collection.json \
     -e poke-go-friends-comprehensive-test.postman_environment.json \
     --env-var "baseUrl=https://api.pokegofriends.dev"
   ```

## 📊 Test Results & Reporting

### Understanding Test Results

**✅ Passing Tests:** All assertions passed successfully
**❌ Failing Tests:** One or more assertions failed
**⚠️ Skipped Tests:** Tests that were skipped due to conditions

### Key Metrics to Monitor

1. **Response Times:**
   - Health endpoints: < 1s
   - CRUD operations: < 3s
   - Search operations: < 5s
   - Large requests: < 10s

2. **Success Rates:**
   - Valid requests: 100% success
   - Invalid requests: Proper error codes
   - Edge cases: Graceful handling

3. **Error Handling:**
   - 400 errors: Proper validation messages
   - 404 errors: Resource not found scenarios
   - 409 errors: Conflict situations
   - 500 errors: Should be minimal/none

## 🔧 Test Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL |
| `testTimeout` | `5000` | Request timeout (ms) |
| `maxRetries` | `3` | Maximum retry attempts |
| `testDataPrefix` | `AutoTest_` | Prefix for test data |
| `cleanupRequired` | `true` | Enable test data cleanup |

### Collection Variables (Auto-Generated)

| Variable | Purpose |
|----------|---------|
| `randomFriendCode` | Dynamically generated friend codes |
| `testTrainerName` | Unique trainer names for testing |
| `createdFriendCodeId` | ID of created friend code for subsequent tests |
| `currentTimestamp` | Current timestamp for time-based testing |

## 🧹 Test Data Management

### Automatic Cleanup
The test suite includes automatic test data management:

1. **Dynamic Data Generation:** Uses Postman's built-in random functions
2. **Unique Identifiers:** Ensures no conflicts between test runs
3. **State Management:** Tracks created resources for proper cleanup
4. **Isolation:** Each test run is independent

### Manual Cleanup
If manual cleanup is needed:

```sql
-- Remove test data (be careful with this in production!)
DELETE FROM friend_codes WHERE trainer_name LIKE 'AutoTest_%';
DELETE FROM friend_codes WHERE trainer_name LIKE 'TestTrainer%';
```

## 🔍 Troubleshooting

### Common Issues

1. **Connection Refused:**
   - Verify API server is running
   - Check `baseUrl` configuration
   - Ensure correct port number

2. **Test Failures:**
   - Check API server logs
   - Verify database connectivity
   - Review validation rules

3. **Performance Issues:**
   - Monitor server resources
   - Check database performance
   - Review timeout settings

### Debug Mode
Enable verbose logging in Newman:
```bash
newman run collection.json -e environment.json --verbose
```

## 📈 Continuous Integration

### GitHub Actions Example
```yaml
name: API Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install Newman
        run: npm install -g newman
      - name: Run API Tests
        run: |
          newman run postman/poke-go-friends-comprehensive-test-suite.postman_collection.json \
            -e postman/poke-go-friends-comprehensive-test.postman_environment.json \
            --env-var "baseUrl=http://localhost:8080" \
            --reporters cli,junit \
            --reporter-junit-export results.xml
```

### Jenkins Pipeline Example
```groovy
pipeline {
    agent any
    stages {
        stage('API Tests') {
            steps {
                sh '''
                    newman run postman/poke-go-friends-comprehensive-test-suite.postman_collection.json \
                      -e postman/poke-go-friends-comprehensive-test.postman_environment.json \
                      --reporters cli,junit \
                      --reporter-junit-export test-results.xml
                '''
            }
            post {
                always {
                    junit 'test-results.xml'
                }
            }
        }
    }
}
```

## 📝 Best Practices

### Writing Tests
1. **Clear Naming:** Use descriptive test names
2. **Proper Assertions:** Test behavior, not implementation
3. **Error Scenarios:** Include negative test cases
4. **Documentation:** Add descriptions to requests
5. **Maintainability:** Use variables for reusable values

### Running Tests
1. **Environment Isolation:** Use separate environments
2. **Data Independence:** Ensure tests don't depend on each other
3. **Regular Execution:** Run tests frequently
4. **Result Review:** Analyze failures promptly
5. **Performance Monitoring:** Track response times

## 🤝 Contributing

To add new tests:

1. **Follow Naming Convention:** Use emoji prefixes for folders
2. **Include Proper Tests:** Add comprehensive assertions
3. **Document Changes:** Update this README
4. **Test Thoroughly:** Verify new tests work correctly
5. **Maintain Structure:** Keep tests organized logically

## 📚 Additional Resources

- [Postman Documentation](https://learning.postman.com/)
- [Newman Documentation](https://github.com/postmanlabs/newman)
- [API Documentation](../swagger/api-docs.json)
- [Pokemon Go Friends API GitHub](https://github.com/pokemon_go_code_sharing_backend)

---

**Last Updated:** January 2025
**Test Suite Version:** 1.0.0
**API Version:** 1.0.0
