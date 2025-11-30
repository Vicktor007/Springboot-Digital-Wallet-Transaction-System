**CurrencyExchanger** Java project, complete with setup instructions for IntelliJ IDEA and links to necessary libraries.

---

```markdown
# History Service

The History Service consumes wallet events from Kafka, processes them idempotently, and maintains a complete transaction history. It provides APIs for querying wallet and user activities.

## 🏗️ Architecture Overview

┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│  Wallet Service │───▶│    Kafka     │───▶│ History Service │
│ (Spring Boot)   │    │              │    │ (Spring Boot)   │
└─────────────────┘    │wallet_events │    └─────────────────┘
         │             └──────────────┘             │
         └──────────────────────────────────────────┘
                   Shared PostgreSQL Database
                   
## 🌟 Features

- Postgres database

- Kafka consumer implementation

- Error handling for failures and kafka connectivity issues

- API documentation for the endpoints

## 🧰 Technologies Used

- Java 21
- Springboot 3.5.6
- Kafka
- Docker
- OpenApi Docs

## 🔌 API Endpoints

History Service (:8081)
GET /wallets/{walletId}/history - Wallet transaction history

GET /users/{userId}/activity - User activity timeline


## API documentation

- history-service: http://localhost:8081/swagger-ui.html


---

