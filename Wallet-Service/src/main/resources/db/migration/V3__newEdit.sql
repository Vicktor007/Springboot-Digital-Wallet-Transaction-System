CREATE TABLE wallet_event_log (
                                  id BIGSERIAL PRIMARY KEY,
                                  event_type VARCHAR(255) NOT NULL,
                                  wallet_id VARCHAR(100),
                                  user_id VARCHAR(100),
                                  amount NUMERIC(19, 4),
                                  sender_id VARCHAR(100),
                                  receiver_id VARCHAR(100),
                                  transaction_id VARCHAR(100),
                                  time_stamp TIMESTAMP,
                                  sent BOOLEAN DEFAULT FALSE,
                                  retry_count INTEGER DEFAULT 0,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
