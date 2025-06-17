-- Create friend_codes table for Pokémon Go friend code sharing
CREATE TABLE friend_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    friend_code VARCHAR(12) NOT NULL,
    trainer_name VARCHAR(50),
    trainer_level INTEGER CHECK (trainer_level >= 1 AND trainer_level <= 50),
    team VARCHAR(20) CHECK (team IN ('INSTINCT', 'MYSTIC', 'VALOR')),
    country VARCHAR(50),
    purpose VARCHAR(20) CHECK (purpose IN ('GIFTS', 'RAIDS', 'BOTH')),
    message VARCHAR(100),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    user_fingerprint VARCHAR(255) NOT NULL
);

-- Create indexes for performance optimization
CREATE INDEX idx_friend_codes_expires_at ON friend_codes(expires_at);
CREATE INDEX idx_friend_codes_submitted_at ON friend_codes(submitted_at);
CREATE INDEX idx_friend_codes_user_fingerprint ON friend_codes(user_fingerprint);

-- Add constraint to ensure friend_code contains only digits
ALTER TABLE friend_codes ADD CONSTRAINT check_friend_code_format 
    CHECK (friend_code ~ '^[0-9]{12}$');
