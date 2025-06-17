These guidelines will help you get the most out of GitHub Copilot when working on a Java Spring Framework backend project, ensuring high code quality, testability, and maintainability.

1. Purpose
- Align Copilot suggestions with project and team standards.
- Encourage best practices in design, implementation, and testing.
- Maintain consistency across the codebase.

2. When to Accept Suggestions
- Adheres to project conventions (naming, formatting, package structure)
- Follows SOLID principles and keeps methods small and focused
- Illustrates clear intent with descriptive identifiers
- Includes appropriate documentation (Javadoc or comments) for public APIs

3. When to Refine or Reject Suggestions
- Complex one-liners that hinder readability
- Leaking implementation details into higher layers such as services or controllers
- Missing error handling or edge-case coverage
- Poor naming of variables or methods
- Nonstandard formatting, including inconsistent indentation or excessive line length

4. Coding Standards
- Follow the Google Java Style Guide (link to official guide).
- Use four-space indentation, with a maximum of 120 characters per line.
- Keep classes and methods small and focused on a single responsibility.
- Favor immutability where practical, for example marking fields as final.
- Leverage Spring idioms: use constructor injection, apply @Service and @Component stereotypes, and minimize field-based @Autowired usage.

5. Testing Guidelines
5.1 Test-Driven Development (TDD)
- Always write a failing test first, then implement production code.
- Ensure tests remain fast, focused, and deterministic.
- Strictly follow the red-green-refactor cycle.

5.2 Unit Tests
- Place tests under src/test/java mirroring the main package structure.
- Use a clear naming convention: ClassNameTest for test classes and methodName_expectedBehavior for test methods.
- Test one class per unit test, mocking external dependencies only when absolutely necessary.
- Focus assertions on the public behavior of the unit under test rather than internal implementation details.

5.3 Integration Tests
- Organize integration tests separately, for example under src/integrationTest/java.
- Name integration test classes with an IT suffix, such as ClassNameIT.
- Load only the required Spring context slices to exercise real components.
- Avoid using mocks and verify end-to-end behavior including HTTP endpoints, transactions, and database integration.
- Reset or rollback shared state between tests by leveraging transactional rollbacks or container restarts.

6. Testing Behavior Over Implementation
- Drive tests through public APIs and interfaces.
- Do not assert on private fields or internal method calls.
- Refactor internal code freely as long as the observable behavior tested remains consistent.

7. Tips for Copilot with Testing
- Prompt Copilot with clear instructions such as writing a test for a specific service method and expected outcome.
- Review suggested mock setups to ensure they match actual method signatures.
- Replace overly generic stubbing with explicit values to improve test clarity and maintainability.