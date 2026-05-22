-- V5: Add password reset token support to customer table
ALTER TABLE customer ADD COLUMN IF NOT EXISTS reset_token VARCHAR(255);
ALTER TABLE customer ADD COLUMN IF NOT EXISTS reset_token_expiry TIMESTAMP;
