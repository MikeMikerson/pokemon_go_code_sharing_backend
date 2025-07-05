-- ROLLBACK SCRIPT FOR V5__Update_trainer_name_constraints.sql
-- 
-- This script provides a way to rollback the trainer name constraint changes
-- if needed. It restores original trainer names from backup tables and
-- reverts schema changes.
--
-- WARNING: This script should only be run if you need to rollback V5 migration
-- DO NOT RUN THIS SCRIPT AS PART OF NORMAL MIGRATION PROCESS
--
-- Prerequisites:
-- 1. Backup tables must exist: friend_codes_trainer_name_backup, users_trainer_name_backup
-- 2. Original V5 migration must have completed successfully
--
-- Usage:
-- Run this script manually via psql or database administration tool
-- when rollback is required.

-- ==============================================================================
-- STEP 1: VERIFY BACKUP TABLES EXIST
-- ==============================================================================

DO $$
BEGIN
    -- Check if backup tables exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'friend_codes_trainer_name_backup') THEN
        RAISE EXCEPTION 'Backup table friend_codes_trainer_name_backup not found. Cannot proceed with rollback.';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'users_trainer_name_backup') THEN
        RAISE EXCEPTION 'Backup table users_trainer_name_backup not found. Cannot proceed with rollback.';
    END IF;
    
    RAISE NOTICE 'Backup tables verified. Proceeding with rollback...';
END $$;

-- ==============================================================================
-- STEP 2: REMOVE CONSTRAINTS
-- ==============================================================================

-- Drop check constraints
ALTER TABLE friend_codes DROP CONSTRAINT IF EXISTS chk_trainer_name_format;
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_trainer_name_format;

RAISE NOTICE 'Check constraints removed.';

-- ==============================================================================
-- STEP 3: RESTORE ORIGINAL COLUMN SIZES
-- ==============================================================================

-- Restore original column sizes to VARCHAR(100)
ALTER TABLE friend_codes ALTER COLUMN trainer_name TYPE VARCHAR(100);
ALTER TABLE users ALTER COLUMN trainer_name TYPE VARCHAR(100);

RAISE NOTICE 'Column sizes restored to VARCHAR(100).';

-- ==============================================================================
-- STEP 4: RESTORE ORIGINAL TRAINER NAMES
-- ==============================================================================

-- Restore original trainer names for friend_codes
UPDATE friend_codes 
SET trainer_name = backup.original_trainer_name
FROM friend_codes_trainer_name_backup backup
WHERE friend_codes.id = backup.id;

-- Restore original trainer names for users
UPDATE users 
SET trainer_name = backup.original_trainer_name
FROM users_trainer_name_backup backup
WHERE users.id = backup.id;

-- ==============================================================================
-- STEP 5: REPORT ROLLBACK RESULTS
-- ==============================================================================

DO $$
DECLARE
    friend_codes_restored INTEGER;
    users_restored INTEGER;
BEGIN
    -- Count restored records
    SELECT COUNT(*) INTO friend_codes_restored 
    FROM friend_codes_trainer_name_backup;
    
    SELECT COUNT(*) INTO users_restored 
    FROM users_trainer_name_backup;
    
    RAISE NOTICE '================================';
    RAISE NOTICE 'ROLLBACK COMPLETED SUCCESSFULLY';
    RAISE NOTICE '================================';
    RAISE NOTICE 'Original trainer names restored:';
    RAISE NOTICE '  - friend_codes table: % records', friend_codes_restored;
    RAISE NOTICE '  - users table: % records', users_restored;
    RAISE NOTICE '';
    RAISE NOTICE 'Schema changes reverted:';
    RAISE NOTICE '  - Column size restored to VARCHAR(100)';
    RAISE NOTICE '  - Check constraints removed';
    RAISE NOTICE '';
    RAISE NOTICE 'Backup tables preserved for audit:';
    RAISE NOTICE '  - friend_codes_trainer_name_backup';
    RAISE NOTICE '  - users_trainer_name_backup';
    RAISE NOTICE '================================';
END $$;

-- ==============================================================================
-- STEP 6: UPDATE DOCUMENTATION
-- ==============================================================================

-- Restore original table comments
COMMENT ON COLUMN friend_codes.trainer_name IS 'Pokemon Go trainer name';
COMMENT ON COLUMN users.trainer_name IS 'Pokemon Go trainer name';

-- Note: Backup tables are preserved for audit purposes
-- They can be manually dropped later if no longer needed:
-- DROP TABLE friend_codes_trainer_name_backup;
-- DROP TABLE users_trainer_name_backup;

RAISE NOTICE 'Rollback script completed. V5 migration changes have been reverted.';
