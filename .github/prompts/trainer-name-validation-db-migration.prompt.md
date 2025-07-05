# Pokemon Go Friends API - Trainer Name Validation Update

I need help with database migration for trainer name validation changes in a Spring Boot application.

## What was changed

- Updated trainer name validation from 100 characters to 20 characters maximum
- Changed from allowing letters, numbers, spaces, and special characters to alphanumeric only (a-z, A-Z, 0-9)
- Updated DTOs, entities, and validation service

## Current database schema

```sql
-- friend_codes table
trainer_name VARCHAR(100)

-- users table  
trainer_name VARCHAR(100)
```

## Target schema

```sql
-- friend_codes table
trainer_name VARCHAR(20) CHECK (trainer_name IS NULL OR trainer_name ~ '^[a-zA-Z0-9]*$')

-- users table
trainer_name VARCHAR(20) CHECK (trainer_name IS NULL OR trainer_name ~ '^[a-zA-Z0-9]*$')
```

## Migration requirements

1. Handle existing data that exceeds 20 characters (truncate to 20)
2. Remove special characters from existing trainer names (keep only alphanumeric)
3. Add database constraints to enforce new rules
4. Ensure migration is safe for production use

## Current migration file started

`V5__Update_trainer_name_constraints.sql`

## Testing shows existing data includes

- "ThisNameIsTooLongForOurNew20CharacterLimit" (45 chars)
- "Test@Name" (contains special characters)

## Code changes made

### DTO Validation (FriendCodeSubmissionRequest.java)
```java
@Size(max = 20, message = "Trainer name cannot exceed 20 characters")
@Pattern(regexp = "^[a-zA-Z0-9]*$", 
         message = "Trainer name can only contain letters and numbers")
@Schema(
    description = "Pokemon Go trainer name",
    example = "PikachuMaster",
    maxLength = 20,
    pattern = "^[a-zA-Z0-9]*$"
)
private String trainerName;
```

### Entity Validation (FriendCode.java)
```java
@Column(name = "trainer_name", nullable = true, length = 20)
@Size(max = 20, message = "Trainer name cannot exceed 20 characters")
@Pattern(regexp = "^[a-zA-Z0-9]*$", 
         message = "Trainer name can only contain letters and numbers")
private String trainerName;
```

### Service Validation (ValidationService.java)
```java
// Enhanced trainer name validation pattern (letters and numbers only)
private static final Pattern TRAINER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

public void validateTrainerName(String trainerName) {
    // ... existing null/empty checks ...
    
    if (sanitized.length() > 20) {
        throw new ValidationException("Trainer name cannot exceed 20 characters");
    }

    if (!TRAINER_NAME_PATTERN.matcher(sanitized).matches()) {
        throw new ValidationException("Trainer name can only contain letters and numbers");
    }
    
    // ... rest of validation ...
}
```

## Files modified

- `src/main/java/com/devs/simplicity/poke_go_friends/dto/FriendCodeSubmissionRequest.java`
- `src/main/java/com/devs/simplicity/poke_go_friends/dto/FriendCodeUpdateRequest.java`
- `src/main/java/com/devs/simplicity/poke_go_friends/dto/FriendCodeResponse.java`
- `src/main/java/com/devs/simplicity/poke_go_friends/entity/FriendCode.java`
- `src/main/java/com/devs/simplicity/poke_go_friends/entity/User.java`
- `src/main/java/com/devs/simplicity/poke_go_friends/service/ValidationService.java`
- Multiple test files updated to match new validation rules

## Request

Please help me create a robust database migration that safely transforms existing data and enforces the new constraints.

The migration should:
1. Be safe for production deployment
2. Handle edge cases gracefully
3. Provide rollback capability if needed
4. Include proper logging/feedback during migration
5. Ensure no data loss (truncate/clean rather than delete records)
