-- Create users table for authentication and user management
-- This migration adds the users table and establishes the foreign key relationship with friend_codes

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    trainer_name VARCHAR(100),
    player_level INTEGER CHECK (player_level >= 1 AND player_level <= 50),
    location VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP
);

-- Add comments for documentation
COMMENT ON TABLE users IS 'Stores user accounts for the Pokemon Go friend code sharing system';
COMMENT ON COLUMN users.id IS 'Primary key, auto-generated';
COMMENT ON COLUMN users.username IS 'Unique username for the account';
COMMENT ON COLUMN users.email IS 'User email address, must be unique';
COMMENT ON COLUMN users.password_hash IS 'Hashed password for security';
COMMENT ON COLUMN users.trainer_name IS 'Pokemon Go trainer name';
COMMENT ON COLUMN users.player_level IS 'Current player level, between 1 and 50';
COMMENT ON COLUMN users.location IS 'User location (city, country)';
COMMENT ON COLUMN users.is_active IS 'Whether the user account is active';
COMMENT ON COLUMN users.email_verified IS 'Whether the user has verified their email';
COMMENT ON COLUMN users.created_at IS 'When the user account was created';
COMMENT ON COLUMN users.updated_at IS 'When the user account was last updated';
COMMENT ON COLUMN users.last_login_at IS 'When the user last logged in';

-- Create indexes for performance
CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created ON users(created_at DESC);
CREATE INDEX idx_users_active ON users(is_active) WHERE is_active = true;
CREATE INDEX idx_users_verified ON users(email_verified) WHERE email_verified = true;

-- Add constraints to ensure data integrity
ALTER TABLE users ADD CONSTRAINT chk_username_format 
    CHECK (username ~ '^[a-zA-Z0-9_]+$');

ALTER TABLE users ADD CONSTRAINT chk_username_not_empty 
    CHECK (LENGTH(TRIM(username)) > 0);

ALTER TABLE users ADD CONSTRAINT chk_email_not_empty 
    CHECK (LENGTH(TRIM(email)) > 0);

ALTER TABLE users ADD CONSTRAINT chk_password_hash_not_empty 
    CHECK (LENGTH(TRIM(password_hash)) > 0);

ALTER TABLE users ADD CONSTRAINT chk_trainer_name_not_empty 
    CHECK (trainer_name IS NULL OR LENGTH(TRIM(trainer_name)) > 0);

ALTER TABLE users ADD CONSTRAINT chk_location_not_empty 
    CHECK (location IS NULL OR LENGTH(TRIM(location)) > 0);

-- Add foreign key constraint to friend_codes table
ALTER TABLE friend_codes ADD CONSTRAINT fk_friend_codes_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create trigger for users table to automatically update updated_at
CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
