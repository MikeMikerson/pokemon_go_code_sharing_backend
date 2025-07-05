# Trainer Name Validation Database Migration (V5)

## Overview

This document describes the database migration `V5__Update_trainer_name_constraints.sql` that updates trainer name validation in the Pokemon Go Friends API from 100 characters to 20 characters maximum and restricts allowed characters to alphanumeric only.

## Migration Summary

### Previous Constraints
- **Maximum length**: 100 characters
- **Allowed characters**: Letters, numbers, spaces, and special characters

### New Constraints
- **Maximum length**: 20 characters
- **Allowed characters**: Letters (a-z, A-Z) and numbers (0-9) only

## Migration Features

### 🛡️ Safety Features
- **Data Preservation**: No data is deleted; invalid entries are cleaned and transformed
- **Rollback Capability**: Backup tables are created for complete rollback if needed
- **Validation**: All data is validated before constraints are applied
- **Audit Trail**: Detailed logging of all transformations for compliance

### 🔧 Transformation Logic
1. **Character Cleaning**: Removes all non-alphanumeric characters (spaces, @, -, etc.)
2. **Length Truncation**: Truncates names longer than 20 characters to exactly 20 characters
3. **Null Handling**: Preserves NULL values and handles empty strings gracefully
4. **Edge Cases**: Names that become empty after cleaning are set to NULL

### 📊 Comprehensive Logging
- Counts and reports affected records before transformation
- Logs each transformation with the reason (truncation, character removal, or both)
- Validates data integrity before applying constraints
- Provides detailed completion summary

## Files Created

### Migration Files
- `V5__Update_trainer_name_constraints.sql` - Main migration script
- `ROLLBACK_V5__trainer_name_constraints.sql` - Rollback script (manual use only)

### Backup Tables Created
- `friend_codes_trainer_name_backup` - Original trainer names from friend_codes table
- `users_trainer_name_backup` - Original trainer names from users table

## Migration Process

### Step-by-Step Execution

1. **Backup Creation**
   ```sql
   -- Creates backup tables with original values and timestamps
   CREATE TABLE friend_codes_trainer_name_backup AS ...
   CREATE TABLE users_trainer_name_backup AS ...
   ```

2. **Data Analysis**
   ```
   FRIEND_CODES TABLE ANALYSIS:
     - Records with trainer names > 20 chars: X
     - Records with special characters: Y  
     - Total records requiring transformation: Z
   ```

3. **Data Transformation**
   ```sql
   -- Safe transformation function applied to both tables
   UPDATE friend_codes SET trainer_name = transform_trainer_name(trainer_name) ...
   UPDATE users SET trainer_name = transform_trainer_name(trainer_name) ...
   ```

4. **Constraint Validation**
   ```
   CONSTRAINT VALIDATION:
     - friend_codes violations after transformation: 0
     - users violations after transformation: 0
     - All data successfully transformed and validated
   ```

5. **Schema Updates**
   ```sql
   -- Column size reduction and constraint addition
   ALTER TABLE friend_codes ALTER COLUMN trainer_name TYPE VARCHAR(20);
   ALTER TABLE friend_codes ADD CONSTRAINT chk_trainer_name_format ...
   ```

## Example Transformations

| Original Trainer Name | Transformed | Reason |
|----------------------|-------------|--------|
| `"ThisNameIsTooLongForOurNew20CharacterLimit"` | `"ThisNameIsTooLongFo"` | Truncated to 20 chars |
| `"Test@Name"` | `"TestName"` | Removed special characters |
| `"VeryLong@Name#123"` | `"VeryLongName123"` | Truncated and cleaned |
| `"@#$%"` | `NULL` | Became empty after cleaning |
| `"ValidName123"` | `"ValidName123"` | No change required |

## Rollback Procedure

If rollback is required, use the provided rollback script:

```sql
-- Manual execution only - NOT part of Flyway migrations
\i ROLLBACK_V5__trainer_name_constraints.sql
```

### Rollback Process
1. Validates backup tables exist
2. Removes new constraints
3. Restores original column sizes (VARCHAR(100))
4. Restores original trainer names from backup tables
5. Updates documentation to reflect rollback

## Verification Commands

### Pre-Migration Analysis
```sql
-- Check current data distribution
SELECT 
    LENGTH(trainer_name) as name_length,
    COUNT(*) as count,
    trainer_name ~ '[^a-zA-Z0-9]' as has_special_chars
FROM friend_codes 
WHERE trainer_name IS NOT NULL 
GROUP BY LENGTH(trainer_name), has_special_chars 
ORDER BY name_length;
```

### Post-Migration Verification
```sql
-- Verify all constraints are satisfied
SELECT COUNT(*) as violations 
FROM friend_codes 
WHERE trainer_name IS NOT NULL 
AND (LENGTH(trainer_name) > 20 OR trainer_name !~ '^[a-zA-Z0-9]*$');

-- Should return 0 violations
```

### Backup Table Inspection
```sql
-- Review transformations that were applied
SELECT 
    table_name,
    transformation_reason,
    COUNT(*) as affected_records
FROM trainer_name_transformation_log 
GROUP BY table_name, transformation_reason;
```

## Production Deployment

### Pre-Deployment Checklist
- [ ] Database backup completed
- [ ] Migration tested on staging environment with production-like data
- [ ] Rollback procedure tested and verified
- [ ] Application code already deployed with new validation rules
- [ ] Monitoring and alerting configured for migration process

### Deployment Command
```bash
# Run migration through normal Flyway process
./gradlew flywayMigrate
```

### Post-Deployment Verification
- [ ] Check migration logs for transformation counts
- [ ] Verify constraint violations = 0
- [ ] Confirm application functionality with new constraints
- [ ] Monitor error logs for any issues

## Troubleshooting

### Common Issues

**Issue**: Migration fails with constraint violations
**Solution**: Check data transformation logic and ensure all edge cases are handled

**Issue**: Application errors after migration
**Solution**: Verify application validation rules match database constraints

**Issue**: Need to rollback migration
**Solution**: Use provided rollback script and validate data restoration

### Contact Information

For issues with this migration, contact the development team or reference:
- Migration file: `V5__Update_trainer_name_constraints.sql`
- Test cases: `FriendCodeSubmissionRequestTest`, `UserValidationTest`
- Related code changes: DTO validation, entity constraints, service validation

## Related Application Changes

This migration supports the following application-level changes:

### DTO Validation
```java
@Size(max = 20, message = "Trainer name cannot exceed 20 characters")
@Pattern(regexp = "^[a-zA-Z0-9]*$", 
         message = "Trainer name can only contain letters and numbers")
private String trainerName;
```

### Entity Constraints
```java
@Column(name = "trainer_name", nullable = true, length = 20)
@Size(max = 20, message = "Trainer name cannot exceed 20 characters")
@Pattern(regexp = "^[a-zA-Z0-9]*$", 
         message = "Trainer name can only contain letters and numbers")
private String trainerName;
```

### Service Validation
```java
private static final Pattern TRAINER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

public void validateTrainerName(String trainerName) {
    if (sanitized.length() > 20) {
        throw new ValidationException("Trainer name cannot exceed 20 characters");
    }
    if (!TRAINER_NAME_PATTERN.matcher(sanitized).matches()) {
        throw new ValidationException("Trainer name can only contain letters and numbers");
    }
}
```
