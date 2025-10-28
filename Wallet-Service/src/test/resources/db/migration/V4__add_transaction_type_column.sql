ALTER TABLE transaction_events
    ADD COLUMN transaction_type VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE wallet_event_log
    ADD COLUMN transaction_type VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN';
