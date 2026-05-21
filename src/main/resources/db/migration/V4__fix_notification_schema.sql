-- V4: Fix notification table to match JPA entity and add is_read support

ALTER TABLE notification ADD COLUMN IF NOT EXISTS type VARCHAR(100);
ALTER TABLE notification ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notification ADD COLUMN IF NOT EXISTS created_date TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE notification ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP;
