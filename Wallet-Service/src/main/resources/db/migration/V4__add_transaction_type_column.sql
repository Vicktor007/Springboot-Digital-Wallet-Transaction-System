-- Add transaction_type column to transaction_events
ALTER TABLE transaction_events
    ADD COLUMN transaction_type VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN';

-- Add transaction_type column to wallet_event_log
ALTER TABLE wallet_event_log
    ADD COLUMN transaction_type VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN';
