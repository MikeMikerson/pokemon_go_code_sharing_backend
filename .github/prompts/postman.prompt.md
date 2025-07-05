You are an expert software engineer specializing in API testing and automation. Your task is to create a comprehensive Postman testing suite for the "Pokemon Go Friend Code Sharing API" based on the provided OpenAPI specification (`api-docs.json`).

Your deliverables will be two JSON files:
1.  A Postman environment file.
2.  A Postman collection file containing all requests and comprehensive tests.

**Instructions:**

**1. Create the Postman Environment**

Generate a Postman environment named `Pokemon Go Friends API`. It should contain a single variable, `baseUrl`, to allow easy switching between servers. Create configurations for:
*   **Local:** `baseUrl` = `http://localhost:8080`
*   **Production:** `baseUrl` = `https://api.pokegofriends.dev`

**2. Generate the Postman Collection**

Generate a Postman collection from the attached `api-docs.json` file. Ensure the following:
*   The collection is named "Pokemon Go Friend Code Sharing API".
*   Requests are organized into folders corresponding to the `tags` in the OpenAPI spec (e.g., "Health", "Friend Codes").
*   All requests use the `{{baseUrl}}` environment variable in their URLs.
*   Request bodies and parameters are pre-filled with the example values from the `api-docs.json` file.

**3. Implement Comprehensive Tests**

For each request in the collection, add detailed tests using JavaScript in the "Tests" tab. The tests must validate status codes, response bodies, and business logic as outlined below.

**Testing Plan:**

**Health Endpoints**
*   **GET /api/health**:
    *   Test for a `200 OK` status.
*   **GET /api/health/live**:
    *   Test for a `200 OK` status.
*   **GET /api/health/ready**:
    *   Test for a `200 OK` status.

**Friend Codes Endpoints**
*   **POST /api/friend-codes (Submit Friend Code)**:
    *   **Success (201)**: On valid submission, verify the response body matches the `FriendCodeResponse` schema and save the created `id` to an environment variable for use in other tests.
    *   **Bad Request (400)**: Test with an invalid friend code format (e.g., not 12 digits).
    *   **Conflict (409)**: Test submitting the same friend code twice.
    *   **Rate Limit (429)**: Test that the rate limit is enforced (this may require multiple rapid requests).
*   **GET /api/friend-codes (Get Friend Codes)**:
    *   **Success (200)**: Verify the response matches the `FriendCodeFeedResponse` schema.
    *   **Pagination**: Test the `page` and `size` parameters.
    *   **Sorting**: Test `sortBy` and `sortDir` (e.g., `createdAt`, `desc`).
    *   **Filtering**: Test `location`, `minLevel`, `maxLevel`, and `search` filters.
*   **GET /api/friend-codes/{id} (Get Friend Code by ID)**:
    *   **Success (200)**: Use the `id` saved from the POST request to fetch a friend code and verify the response.
    *   **Not Found (404)**: Test with a non-existent ID (e.g., `999999`).
*   **GET /api/friend-codes/stats**:
    *   **Success (200)**: Verify the response status and check for the presence of expected statistical fields.
*   **GET /api/friend-codes/search**:
    *   **Success (200)**: Test with various search parameters (`trainerName`, `location`, etc.) and verify the results are accurate.
*   **GET /api/friend-codes/recent**:
    *   **Success (200)**: Test with the `hours` parameter and verify that only recently created codes are returned.

Please provide the final Postman environment and collection as two separate JSON files and put them in the `postman` directory. Ensure the files are well-structured and follow Postman's best practices for organization and readability. use context7