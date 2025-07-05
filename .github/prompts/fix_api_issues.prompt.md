---
mode: 'agent'
description: 'Fixes API issues based on Postman test results and API specification.'
---

Please address the failing API tests identified in the [Postman test run](../../Pokemon Go Friends API - Comprehensive Test Suite.postman_test_run.json).

Refer to the [API documentation](../../swagger/api-docs.json) for the expected behavior and the [coding instructions](../copilot-instructions.md) for standards.

The primary goals are to improve the robustness of the API by enhancing input validation, providing clearer error responses, and ensuring all features function according to the specification.

## Plan of Action

### 1. Enhance Input Validation and Error Handling
- **Create a Global Exception Handler:** Implement a `@ControllerAdvice` class to centralize exception handling. This class will catch specific exceptions and translate them into consistent, user-friendly JSON error responses.
- **Handle Validation Errors:** Create handlers for `MethodArgumentNotValidException` and `ConstraintViolationException` to return a `400 Bad Request` status with details about the validation failures.
- **Handle Invalid Content-Type:** Add an exception handler for `HttpMediaTypeNotSupportedException` to return a `415 Unsupported Media Type` error.
- **Review DTOs:** Add or correct validation annotations (`@NotNull`, `@Size`, `@Pattern`, `@Min`, `@Max`) in all Data Transfer Objects (DTOs) to ensure incoming data is validated at the earliest stage.

### 2. Improve Friend Code Submission Logic
- **Sanitize Friend Code Input:** In the `FriendCodeDTO`, implement a custom setter or use a pre-processing step in the service layer to remove any spaces or dashes from the `friendCode` string before it is processed and stored.
- **Implement Duplicate Check:** In the `FriendCodeService`, before saving a new friend code, query the database to check if a code with the same value already exists. If a duplicate is found, throw a custom exception (e.g., `DuplicateFriendCodeException`) that the global exception handler will map to a `409 Conflict` response.

### 3. Fix Filtering and Searching
- **Repair Filtering Logic:** The Postman results indicate that filtering by `location` and level range (`minLevel`, `maxLevel`) causes a `500 Internal Server Error`. This points to a bug in the repository or service layer where the database query is constructed. Debug the `Specification` or `QueryDSL` logic in the `FriendCodeRepository` to correctly build and execute the query for these filter parameters.
- **Validate Pagination Parameters:** Ensure that pagination parameters like `page` and `size` are validated to prevent invalid values (e.g., negative numbers or excessively large sizes).

### 4. Correct Deletion Behavior
- **Ensure Correct HTTP Status:** The `DELETE /api/friend-codes/{id}` endpoint should return a `204 No Content` response upon successful deletion, as is standard for this operation. The controller method should be annotated with `@ResponseStatus(HttpStatus.NO_CONTENT)`. The Postman test script should be updated to assert this status code without expecting a response body.

Please implement these changes, following the TDD methodology outlined in the instructions. Write failing tests first, then implement the code to make them pass.
