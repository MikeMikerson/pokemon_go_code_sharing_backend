-- Update trainer_name column constraints for both friend_codes and users tables
-- Limit trainer names to 20 characters and enforce alphanumeric only

-- First, update existing trainer names that are too long or contain invalid characters
-- Truncate trainer names that are longer than 20 characters
UPDATE friend_codes 
SET trainer_name = LEFT(trainer_name, 20) 
WHERE trainer_name IS NOT NULL AND LENGTH(trainer_name) > 20;

UPDATE users 
SET trainer_name = LEFT(trainer_name, 20) 
WHERE trainer_name IS NOT NULL AND LENGTH(trainer_name) > 20;

-- Remove non-alphanumeric characters from existing trainer names
UPDATE friend_codes 
SET trainer_name = REGEXP_REPLACE(trainer_name, '[^a-zA-Z0-9]', '', 'g') 
WHERE trainer_name IS NOT NULL AND trainer_name ~ '[^a-zA-Z0-9]';

UPDATE users 
SET trainer_name = REGEXP_REPLACE(trainer_name, '[^a-zA-Z0-9]', '', 'g') 
WHERE trainer_name IS NOT NULL AND trainer_name ~ '[^a-zA-Z0-9]';

-- Alter the column size for friend_codes table
ALTER TABLE friend_codes ALTER COLUMN trainer_name TYPE VARCHAR(20);

-- Alter the column size for users table  
ALTER TABLE users ALTER COLUMN trainer_name TYPE VARCHAR(20);

-- Add check constraints to enforce alphanumeric only format
ALTER TABLE friend_codes ADD CONSTRAINT chk_trainer_name_format 
    CHECK (trainer_name IS NULL OR trainer_name ~ '^[a-zA-Z0-9]*$');

ALTER TABLE users ADD CONSTRAINT chk_users_trainer_name_format 
    CHECK (trainer_name IS NULL OR trainer_name ~ '^[a-zA-Z0-9]*$');

-- Update table comments
COMMENT ON COLUMN friend_codes.trainer_name IS 'Pokemon Go trainer name (max 20 alphanumeric characters)';
COMMENT ON COLUMN users.trainer_name IS 'Pokemon Go trainer name (max 20 alphanumeric characters)';
