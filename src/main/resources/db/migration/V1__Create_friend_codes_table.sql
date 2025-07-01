-- Initial schema migration for Pokemon Go Code Sharing Backend
-- Creates the friend_codes table with all necessary columns and constraints

CREATE TABLE friend_codes (
    id BIGSERIAL PRIMARY KEY,
    friend_code VARCHAR(12) NOT NULL UNIQUE,
    trainer_name VARCHAR(100) NOT NULL,
    player_level INTEGER CHECK (player_level >= 1 AND player_level <= 50),
    location VARCHAR(200),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    user_id BIGINT
);

-- Add comments for documentation
COMMENT ON TABLE friend_codes IS 'Stores Pokemon Go friend codes shared by users';
COMMENT ON COLUMN friend_codes.id IS 'Primary key, auto-generated';
COMMENT ON COLUMN friend_codes.friend_code IS 'Pokemon Go friend code, exactly 12 digits';
COMMENT ON COLUMN friend_codes.trainer_name IS 'Pokemon Go trainer name';
COMMENT ON COLUMN friend_codes.player_level IS 'Current player level, between 1 and 50';
COMMENT ON COLUMN friend_codes.location IS 'User location (city, country)';
COMMENT ON COLUMN friend_codes.description IS 'What the user is looking for (gifts, raids, etc.)';
COMMENT ON COLUMN friend_codes.is_active IS 'Whether the friend code is still active/visible';
COMMENT ON COLUMN friend_codes.created_at IS 'When the friend code was first submitted';
COMMENT ON COLUMN friend_codes.updated_at IS 'When the friend code was last updated';
COMMENT ON COLUMN friend_codes.expires_at IS 'Optional expiration date for the friend code';
COMMENT ON COLUMN friend_codes.user_id IS 'Optional reference to the user who submitted this friend code';

-- Create indexes for performance
CREATE INDEX idx_friend_codes_active ON friend_codes(is_active) WHERE is_active = true;
CREATE INDEX idx_friend_codes_created ON friend_codes(created_at DESC);
CREATE INDEX idx_friend_codes_location ON friend_codes(location) WHERE location IS NOT NULL;
CREATE INDEX idx_friend_codes_player_level ON friend_codes(player_level) WHERE player_level IS NOT NULL;
CREATE INDEX idx_friend_codes_expires_at ON friend_codes(expires_at) WHERE expires_at IS NOT NULL;
CREATE INDEX idx_friend_codes_user_id ON friend_codes(user_id) WHERE user_id IS NOT NULL;

-- Create a composite index for common queries (active codes, newest first)
CREATE INDEX idx_friend_codes_active_created ON friend_codes(is_active, created_at DESC) WHERE is_active = true;

-- Create a functional index for case-insensitive location searches
CREATE INDEX idx_friend_codes_location_lower ON friend_codes(LOWER(location)) WHERE location IS NOT NULL;

-- Add constraint to ensure friend_code is exactly 12 digits
ALTER TABLE friend_codes ADD CONSTRAINT chk_friend_code_format 
    CHECK (friend_code ~ '^[0-9]{12}$');

-- Add constraint to ensure trainer_name is not empty
ALTER TABLE friend_codes ADD CONSTRAINT chk_trainer_name_not_empty 
    CHECK (LENGTH(TRIM(trainer_name)) > 0);

-- Add constraint to ensure location is not empty if provided
ALTER TABLE friend_codes ADD CONSTRAINT chk_location_not_empty 
    CHECK (location IS NULL OR LENGTH(TRIM(location)) > 0);

-- Add constraint to ensure description is not empty if provided
ALTER TABLE friend_codes ADD CONSTRAINT chk_description_not_empty 
    CHECK (description IS NULL OR LENGTH(TRIM(description)) > 0);

-- Add constraint to ensure expires_at is in the future when set
ALTER TABLE friend_codes ADD CONSTRAINT chk_expires_at_future 
    CHECK (expires_at IS NULL OR expires_at > created_at);
