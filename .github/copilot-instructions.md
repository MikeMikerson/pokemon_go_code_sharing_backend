# GitHub Copilot Instructions
These guidelines will help you get the most out of GitHub Copilot when working on a Java Spring Framework backend project, ensuring high code quality, testability, and maintainability.

## MCP - interactive-feedback-mcp
Whenever you want to ask a question, always call the MCP interactive-feedback-mcp.
Whenever you’re about to complete a user request, call the MCP interactive-feedback-mcp instead of simply ending the process.
Keep calling MCP until the user’s feedback is empty, then end the request.

## Coding Standards
- Follow Java 21 coding practices. When unsure, look up the latest Java documentation.
- Use the latest Spring Framework version compatible with Java 21.
- Keep code DRY, clean, and maintainable.
- Follow SOLID principles: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion.
- Follow Effective Java practices, such as using static factory methods, avoiding unnecessary object creation, and using enums instead of int constants.
- Use four-space indentation, with a maximum of 120 characters per line.
- Keep classes and methods small and focused on a single responsibility.
- Favor immutability where practical, for example marking fields as final.
- Leverage Spring idioms: use constructor injection, apply @Service and @Component stereotypes, and minimize field-based @Autowired usage.

## Testing Guidelines
### Test-Driven Development (TDD)
- Use ./gradlew clean test to run tests.
- Do not use VSC's "Run Test" feature; always run tests from the command line.
- Always write a failing test first, then implement production code.
- Ensure tests remain fast, focused, and deterministic.
- Strictly follow the red-green-refactor cycle.

### Unit Tests
- Place tests under src/test/java mirroring the main package structure.
- Use a clear naming convention: ClassNameTest for test classes and methodName_expectedBehavior for test methods.
- Test one class per unit test, mocking external dependencies only when absolutely necessary.
- Focus assertions on the public behavior of the unit under test rather than internal implementation details.

### Integration Tests
- Organize integration tests separately, for example under src/integrationTest/java.
- Name integration test classes with an IT suffix, such as ClassNameIT.
- Load only the required Spring context slices to exercise real components.
- Avoid using mocks and verify end-to-end behavior including HTTP endpoints, transactions, and database integration.
- Reset or rollback shared state between tests by leveraging transactional rollbacks or container restarts.

## Testing Behavior Over Implementation
- Drive tests through public APIs and interfaces.
- Do not assert on private fields or internal method calls.
- Refactor internal code freely as long as the observable behavior tested remains consistent.