

-- Clear existing test data
DELETE FROM transaction_events;

-- Insert mock transaction events with CORRECT event types from your enum
INSERT INTO transaction_events (id, wallet_id, user_id, amount, event_type, transaction_id, transaction_type, created_at, event_data) VALUES
-- Wallet creation events
('event-1', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 0.00, 'WALLET_CREATED', 'create-1', 'CREATE_WALLET', '2024-01-15 10:00:00', '{"initialBalance": 0.00, "currency": "USD"}'),

-- Funding events
('event-2', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 100.50, 'WALLET_FUNDED', 'fund-1', 'FUND', '2024-01-15 11:30:00', '{"previousBalance": 0.00, "newBalance": 100.50, "fundingSource": "BANK_TRANSFER"}'),
('event-3', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 50.00, 'WALLET_FUNDED', 'fund-2', 'FUND', '2024-01-16 09:15:00', '{"previousBalance": 100.50, "newBalance": 150.50, "fundingSource": "CREDIT_CARD"}'),

-- Transfer completed events (using direction in event_data)
('event-4', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 25.75, 'TRANSFER_COMPLETED', 'transfer-1', 'TRANSFER_OUT', '2024-01-15 14:45:00', '{"direction": "OUTGOING", "recipientWalletId": "wallet-456", "previousBalance": 100.50, "newBalance": 74.75, "fee": 0.00}'),
('event-5', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 30.00, 'TRANSFER_COMPLETED', 'transfer-3', 'TRANSFER_OUT', '2024-01-17 13:20:00', '{"direction": "OUTGOING", "recipientWalletId": "wallet-999", "previousBalance": 144.75, "newBalance": 114.75, "fee": 0.00}'),
('event-6', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 10.25, 'TRANSFER_COMPLETED', 'transfer-2', 'TRANSFER_IN', '2024-01-16 16:20:00', '{"direction": "INCOMING", "senderWalletId": "wallet-789", "previousBalance": 74.75, "newBalance": 85.00}'),
('event-7', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 15.50, 'TRANSFER_COMPLETED', 'transfer-4', 'TRANSFER_IN', '2024-01-18 08:45:00', '{"direction": "INCOMING", "senderWalletId": "wallet-111", "previousBalance": 114.75, "newBalance": 130.25}'),

-- Failed transaction events (using your exact enum names)
('event-8', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 200.00, 'WALLET_FUNDING_FAILED', 'fund-fail-1', 'FUND', '2024-01-16 11:00:00', '{"error": "INSUFFICIENT_FUNDS", "attemptedAmount": 200.00, "reason": "Bank account had insufficient funds"}'),
('event-9', '8b7d98f8-562d-498e-a350-3b0969d5dc44', 'userId234', 75.00, 'TRANSFER_FAILED', 'transfer-fail-1', 'TRANSFER_OUT', '2024-01-17 16:30:00', '{"error": "INSUFFICIENT_BALANCE", "recipientWalletId": "wallet-777", "attemptedAmount": 75.00, "currentBalance": 44.75}'),

-- Different wallet for user
('event-10', 'wallet-abc-123', 'userId234', 0.00, 'WALLET_CREATED', 'create-2', 'CREATE_WALLET', '2024-01-18 14:00:00', '{"initialBalance": 0.00, "currency": "EUR"}'),
('event-11', 'wallet-abc-123', 'userId234', 500.00, 'WALLET_FUNDED', 'fund-3', 'FUND', '2024-01-18 15:30:00', '{"previousBalance": 0.00, "newBalance": 500.00, "fundingSource": "BANK_TRANSFER", "currency": "EUR"}'),

-- Different user events
('event-12', 'wallet-xyz-789', 'user567', 0.00, 'WALLET_CREATED', 'create-3', 'CREATE_WALLET', '2024-01-16 08:00:00', '{"initialBalance": 0.00, "currency": "USD"}'),
('event-13', 'wallet-xyz-789', 'user567', 75.25, 'WALLET_FUNDED', 'fund-4', 'FUND', '2024-01-16 10:15:00', '{"previousBalance": 0.00, "newBalance": 75.25, "fundingSource": "DEBIT_CARD"}');