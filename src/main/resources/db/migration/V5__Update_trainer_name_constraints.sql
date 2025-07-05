-- Update trainer_name column constraints for both friend_codes and users tables
-- Migration: Limit trainer names to 20 characters and enforce alphanumeric only
-- 
-- This migration safely transforms existing data while adding new constraints:
-- 1. Preserves data by cleaning rather than deleting invalid entries
-- 2. Provides detailed logging for rollback purposes
-- 3. Handles edge cases gracefully
-- 4. Ensures no constraint violations after data transformation

-- ==============================================================================
-- STEP 1: CREATE BACKUP TABLES FOR ROLLBACK CAPABILITY
-- ==============================================================================

-- Create backup tables to store original values for potential rollback
CREATE TABLE friend_codes_trainer_name_backup AS 
SELECT id, trainer_name AS original_trainer_name, now() AS backup_timestamp
FROM friend_codes 
WHERE trainer_name IS NOT NULL;

CREATE TABLE users_trainer_name_backup AS 
SELECT id, trainer_name AS original_trainer_name, now() AS backup_timestamp  
FROM users 
WHERE trainer_name IS NOT NULL;

-- ==============================================================================
-- STEP 2: CREATE HELPER FUNCTION FOR SAFE TRAINER NAME TRANSFORMATION
-- ==============================================================================

-- Function to safely transform trainer names following business rules
CREATE OR REPLACE FUNCTION transform_trainer_name(input_name TEXT) 
RETURNS TEXT AS $$
BEGIN
    -- Return NULL if input is NULL or empty
    IF input_name IS NULL OR TRIM(input_name) = '' THEN
        RETURN NULL;
    END IF;
    
    -- Clean: remove non-alphanumeric characters first
    input_name := REGEXP_REPLACE(input_name, '[^a-zA-Z0-9]', '', 'g');
    
    -- Handle edge case: if cleaning removes all characters, return NULL
    IF input_name = '' THEN
        RETURN NULL;
    END IF;
    
    -- Truncate to 20 characters if necessary
    IF LENGTH(input_name) > 20 THEN
        input_name := LEFT(input_name, 20);
    END IF;
    
    RETURN input_name;
END;
$$ LANGUAGE plpgsql;

-- ==============================================================================
-- STEP 3: LOG TRANSFORMATIONS FOR AUDIT TRAIL
-- ==============================================================================

-- Create temporary tables to log all transformations for audit purposes
CREATE TEMPORARY TABLE trainer_name_transformation_log (
    table_name TEXT,
    record_id BIGINT,
    original_trainer_name TEXT,
    transformed_trainer_name TEXT,
    transformation_reason TEXT,
    transformation_timestamp TIMESTAMP DEFAULT NOW()
);

-- ==============================================================================
-- STEP 4: TRANSFORM EXISTING DATA IN FRIEND_CODES TABLE
-- ==============================================================================

-- Log and count records that will be affected
DO $$
DECLARE
    records_too_long INTEGER;
    records_with_special_chars INTEGER;
    records_both_issues INTEGER;
    total_affected INTEGER;
BEGIN
    -- Count records with different types of issues
    SELECT COUNT(*) INTO records_too_long 
    FROM friend_codes 
    WHERE trainer_name IS NOT NULL AND LENGTH(trainer_name) > 20;
    
    SELECT COUNT(*) INTO records_with_special_chars 
    FROM friend_codes 
    WHERE trainer_name IS NOT NULL AND trainer_name ~ '[^a-zA-Z0-9]';
    
    SELECT COUNT(*) INTO records_both_issues 
    FROM friend_codes 
    WHERE trainer_name IS NOT NULL 
    AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');
    
    SELECT COUNT(*) INTO total_affected 
    FROM friend_codes 
    WHERE trainer_name IS NOT NULL 
    AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');
    
    -- Log the analysis
    RAISE NOTICE 'FRIEND_CODES TABLE ANALYSIS:';
    RAISE NOTICE '  - Records with trainer names > 20 chars: %', records_too_long;
    RAISE NOTICE '  - Records with special characters: %', records_with_special_chars;
    RAISE NOTICE '  - Total records requiring transformation: %', total_affected;
    
    IF total_affected = 0 THEN
        RAISE NOTICE '  - No data transformation needed for friend_codes table';
    END IF;
END $$;

-- Insert transformation log entries for friend_codes
INSERT INTO trainer_name_transformation_log (
    table_name, record_id, original_trainer_name, 
    transformed_trainer_name, transformation_reason
)
SELECT 
    'friend_codes',
    id,
    trainer_name,
    transform_trainer_name(trainer_name),
    CASE 
        WHEN LENGTH(trainer_name) > 20 AND trainer_name ~ '[^a-zA-Z0-9]' 
            THEN 'Truncated to 20 chars and removed special characters'
        WHEN LENGTH(trainer_name) > 20 
            THEN 'Truncated to 20 characters'
        WHEN trainer_name ~ '[^a-zA-Z0-9]' 
            THEN 'Removed special characters'
        ELSE 'No change required'
    END
FROM friend_codes 
WHERE trainer_name IS NOT NULL 
AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');

-- Apply transformations to friend_codes
UPDATE friend_codes 
SET trainer_name = transform_trainer_name(trainer_name)
WHERE trainer_name IS NOT NULL 
AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');

-- ==============================================================================
-- STEP 5: TRANSFORM EXISTING DATA IN USERS TABLE  
-- ==============================================================================

-- Log and count records that will be affected
DO $$
DECLARE
    records_too_long INTEGER;
    records_with_special_chars INTEGER;
    total_affected INTEGER;
BEGIN
    -- Count records with different types of issues
    SELECT COUNT(*) INTO records_too_long 
    FROM users 
    WHERE trainer_name IS NOT NULL AND LENGTH(trainer_name) > 20;
    
    SELECT COUNT(*) INTO records_with_special_chars 
    FROM users 
    WHERE trainer_name IS NOT NULL AND trainer_name ~ '[^a-zA-Z0-9]';
    
    SELECT COUNT(*) INTO total_affected 
    FROM users 
    WHERE trainer_name IS NOT NULL 
    AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');
    
    -- Log the analysis
    RAISE NOTICE 'USERS TABLE ANALYSIS:';
    RAISE NOTICE '  - Records with trainer names > 20 chars: %', records_too_long;
    RAISE NOTICE '  - Records with special characters: %', records_with_special_chars;
    RAISE NOTICE '  - Total records requiring transformation: %', total_affected;
    
    IF total_affected = 0 THEN
        RAISE NOTICE '  - No data transformation needed for users table';
    END IF;
END $$;

-- Insert transformation log entries for users
INSERT INTO trainer_name_transformation_log (
    table_name, record_id, original_trainer_name, 
    transformed_trainer_name, transformation_reason
)
SELECT 
    'users',
    id,
    trainer_name,
    transform_trainer_name(trainer_name),
    CASE 
        WHEN LENGTH(trainer_name) > 20 AND trainer_name ~ '[^a-zA-Z0-9]' 
            THEN 'Truncated to 20 chars and removed special characters'
        WHEN LENGTH(trainer_name) > 20 
            THEN 'Truncated to 20 characters'
        WHEN trainer_name ~ '[^a-zA-Z0-9]' 
            THEN 'Removed special characters'
        ELSE 'No change required'
    END
FROM users 
WHERE trainer_name IS NOT NULL 
AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');

-- Apply transformations to users
UPDATE users 
SET trainer_name = transform_trainer_name(trainer_name)
WHERE trainer_name IS NOT NULL 
AND (LENGTH(trainer_name) > 20 OR trainer_name ~ '[^a-zA-Z0-9]');

-- ==============================================================================
-- STEP 6: VALIDATE DATA BEFORE APPLYING CONSTRAINTS
-- ==============================================================================

-- Verify no records violate the new constraints
DO $$
DECLARE
    friend_codes_violations INTEGER;
    users_violations INTEGER;
BEGIN
    -- Check for constraint violations in friend_codes
    SELECT COUNT(*) INTO friend_codes_violations
    FROM friend_codes 
    WHERE trainer_name IS NOT NULL 
    AND (LENGTH(trainer_name) > 20 OR trainer_name !~ '^[a-zA-Z0-9]*$');
    
    -- Check for constraint violations in users
    SELECT COUNT(*) INTO users_violations
    FROM users 
    WHERE trainer_name IS NOT NULL 
    AND (LENGTH(trainer_name) > 20 OR trainer_name !~ '^[a-zA-Z0-9]*$');
    
    -- Report validation results
    RAISE NOTICE 'CONSTRAINT VALIDATION:';
    RAISE NOTICE '  - friend_codes violations after transformation: %', friend_codes_violations;
    RAISE NOTICE '  - users violations after transformation: %', users_violations;
    
    -- Fail migration if any violations remain
    IF friend_codes_violations > 0 OR users_violations > 0 THEN
        RAISE EXCEPTION 'Data transformation incomplete - constraint violations still exist. Migration aborted.';
    ELSE
        RAISE NOTICE '  - All data successfully transformed and validated';
    END IF;
END $$;

-- ==============================================================================
-- STEP 7: APPLY SCHEMA CHANGES
-- ==============================================================================

-- Alter column sizes (safe since we've already validated the data)
ALTER TABLE friend_codes ALTER COLUMN trainer_name TYPE VARCHAR(20);
ALTER TABLE users ALTER COLUMN trainer_name TYPE VARCHAR(20);

-- Add check constraints to enforce alphanumeric only format
ALTER TABLE friend_codes ADD CONSTRAINT chk_trainer_name_format 
    CHECK (trainer_name IS NULL OR trainer_name ~ '^[a-zA-Z0-9]*$');

ALTER TABLE users ADD CONSTRAINT chk_users_trainer_name_format 
    CHECK (trainer_name IS NULL OR trainer_name ~ '^[a-zA-Z0-9]*$');

-- ==============================================================================
-- STEP 8: UPDATE DOCUMENTATION
-- ==============================================================================

-- Update table comments to reflect new constraints
COMMENT ON COLUMN friend_codes.trainer_name IS 'Pokemon Go trainer name (max 20 alphanumeric characters only)';
COMMENT ON COLUMN users.trainer_name IS 'Pokemon Go trainer name (max 20 alphanumeric characters only)';

-- Document the backup tables for rollback procedures
COMMENT ON TABLE friend_codes_trainer_name_backup IS 'Backup of original trainer_name values before V5 migration for rollback purposes';
COMMENT ON TABLE users_trainer_name_backup IS 'Backup of original trainer_name values before V5 migration for rollback purposes';

-- ==============================================================================
-- STEP 9: CLEANUP AND FINAL REPORTING
-- ==============================================================================

-- Drop the helper function as it's no longer needed
DROP FUNCTION transform_trainer_name(TEXT);

-- Display final migration summary
DO $$
DECLARE
    total_transformations INTEGER;
    friend_codes_transformations INTEGER;
    users_transformations INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_transformations FROM trainer_name_transformation_log;
    SELECT COUNT(*) INTO friend_codes_transformations FROM trainer_name_transformation_log WHERE table_name = 'friend_codes';
    SELECT COUNT(*) INTO users_transformations FROM trainer_name_transformation_log WHERE table_name = 'users';
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'MIGRATION V5 COMPLETED SUCCESSFULLY';
    RAISE NOTICE '================================';
    RAISE NOTICE 'Total trainer name transformations: %', total_transformations;
    RAISE NOTICE '  - friend_codes table: %', friend_codes_transformations;
    RAISE NOTICE '  - users table: %', users_transformations;
    RAISE NOTICE '';
    RAISE NOTICE 'New constraints applied:';
    RAISE NOTICE '  - Maximum length: 20 characters';
    RAISE NOTICE '  - Allowed characters: letters (a-z, A-Z) and numbers (0-9) only';
    RAISE NOTICE '';
    RAISE NOTICE 'Backup tables created for rollback:';
    RAISE NOTICE '  - friend_codes_trainer_name_backup';
    RAISE NOTICE '  - users_trainer_name_backup';
    RAISE NOTICE '================================';
END $$;
