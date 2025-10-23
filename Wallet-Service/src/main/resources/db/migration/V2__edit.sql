CREATE TABLE transaction_events
(
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_data JSONB
);

ALTER TABLE wallet_transactions
    ADD COLUMN sender_id VARCHAR(100) DEFAULT 'unknown',
    ADD COLUMN receiver_id VARCHAR(100) DEFAULT 'unknown';