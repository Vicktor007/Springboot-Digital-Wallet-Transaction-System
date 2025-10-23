**CurrencyExchanger** Java project, complete with setup instructions for IntelliJ IDEA and links to necessary libraries.

---

```markdown
# Digital Wallet Transaction System

A distributed system demonstrating PostgreSQL transactions and Kafka event-driven architecture for digital wallet operations.

## 🏗️ Architecture Overview

┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│  Wallet Service │───▶│    Kafka     │───▶│ History Service │
│ (Spring Boot)   │    │              │    │ (Spring Boot)   │
└─────────────────┘    │wallet_events │    └─────────────────┘
         │             └──────────────┘             │
         └──────────────────────────────────────────┘
                   Shared PostgreSQL Database
                   
## 🌟 Features

- Two microservices using a shared database

- Wallet-service: Core service handling wallet operations, balance management, and financial transactions with immediate consistency.

- History-service: Event-driven service that processes wallet transactions from Kafka and builds an audit trail with eventual consistency.

- Postgres database

- Kafka implementation

- Error handling for failures and kafka connectivity issues

- API documentation for both services

## 🧰 Technologies Used

- Java 21
- Springboot 3.5.6
- Kafka
- Docker
- OpenApi Docs

## 📦 Project Structure

digital-wallet-system/
├── gateway-service/         # centralized endpoints for both microservices 
├── history-service/         # Event processing & audit
├── wallet-service/          # Core wallet operations and event sending
├── docker/compose.yml       # Infrastructure setup
└── logs/                    # Error logs storage

## database schema

# Database Schema

## Complete Schema Definition

```sql
-- Wallets table - stores wallet information and balances
CREATE TABLE wallets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wallet transactions table - records all financial transactions
CREATE TABLE wallet_transactions (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'FUND', 'TRANSFER_OUT', 'TRANSFER_IN'
    status VARCHAR(20) NOT NULL, -- 'COMPLETED', 'FAILED'
    sender_id VARCHAR(100) DEFAULT 'unknown',
    receiver_id VARCHAR(100) DEFAULT 'unknown',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

-- Transaction events table - event-sourced history of all wallet activities
CREATE TABLE transaction_events (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    transaction_id VARCHAR(36),
    transaction_type VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN',
    event_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wallet event log table - audit log for Kafka events and retry mechanism
CREATE TABLE wallet_event_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    wallet_id VARCHAR(100),
    user_id VARCHAR(100),
    amount NUMERIC(19, 4),
    sender_id VARCHAR(100),
    receiver_id VARCHAR(100),
    transaction_id VARCHAR(100),
    transaction_type VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN',
    time_stamp TIMESTAMP,
    sent BOOLEAN DEFAULT FALSE,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Table Relationships

- **wallets** ← **wallet_transactions** (One-to-Many)
- **wallets** → **transaction_events** (One-to-Many)
- **wallets** → **wallet_event_log** (One-to-Many)


## Schema Notes

- All monetary amounts use `DECIMAL(19,4)` for precise financial calculations
- UUIDs (`VARCHAR(36)`) are used for primary keys across tables
- The `version` field in `wallets` enables optimistic locking for concurrent updates
- `transaction_events` uses JSONB for flexible event data storage
- `wallet_event_log` provides retry capability for failed Kafka messages

## 🔌 API Endpoints

Wallet Service (:8080)
POST /wallets - Create new wallet

POST /wallets/{id}/fund - Add funds

POST /wallets/{id}/transfer - Transfer money

GET /wallets/{id} - Get wallet details

GET /users/{userId}/wallets - List user wallets

History Service (:8081)
GET /wallets/{walletId}/history - Wallet transaction history

GET /users/{userId}/activity - User activity timeline


## API documentation

- gateway-service: http://localhost:8085/swagger-ui.html (you can view both services from here)
- wallet-service: http://localhost:8080/swagger-ui.html
- history-service: http://localhost:8081/swagger-ui.html




This project has a major trade off: the messages sent to the kafka consumer will be marked as acknowledged even when they failed to process.
This is handled in the wallet service producer by saving the unsent messages into an eventlog to be sent later every 5 hours. 
I can't think of a better solution to this in the history service with the consumer and
dead letter queue is not considered to fix this issue because Kafka doesn't resend the unprocessed messages automatically; it just saves them.

