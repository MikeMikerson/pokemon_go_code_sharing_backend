-- Create trigger function to automatically update updated_at timestamp

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to call the function on UPDATE
CREATE TRIGGER trigger_friend_codes_updated_at
    BEFORE UPDATE ON friend_codes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comment for documentation
COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates the updated_at timestamp when a row is modified';
