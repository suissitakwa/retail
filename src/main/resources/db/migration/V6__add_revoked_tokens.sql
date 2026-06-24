-- V6: Refresh token revocation store
CREATE TABLE revoked_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    token_hash  VARCHAR(64)  NOT NULL UNIQUE,   -- SHA-256 hex of the JWT string
    revoked_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_revoked_tokens_hash ON revoked_tokens(token_hash);
