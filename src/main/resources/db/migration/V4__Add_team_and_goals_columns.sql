-- Add team and goals columns to friend_codes table
-- Team is an enum value stored as VARCHAR
-- Goals are stored in a separate table for many-to-many relationship

-- Add team column to friend_codes table
ALTER TABLE friend_codes 
ADD COLUMN team VARCHAR(20);

-- Create friend_code_goals table for storing goals
CREATE TABLE friend_code_goals (
    friend_code_id BIGINT NOT NULL,
    goal VARCHAR(20) NOT NULL,
    FOREIGN KEY (friend_code_id) REFERENCES friend_codes(id) ON DELETE CASCADE,
    PRIMARY KEY (friend_code_id, goal)
);

-- Create index for performance
CREATE INDEX idx_friend_code_goals_friend_code_id ON friend_code_goals(friend_code_id);
