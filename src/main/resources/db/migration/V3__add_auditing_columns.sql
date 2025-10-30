-- V3: Add missing auditing columns to the 'payment' table
-- This is necessary to satisfy the JPA entity mapping, which expects
-- 'created_date' and 'last_modified_date' to exist on the table.

-- Add created_date
ALTER TABLE payment
ADD COLUMN created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- Add last_modified_date
ALTER TABLE payment
ADD COLUMN last_modified_date TIMESTAMP WITH TIME ZONE;
