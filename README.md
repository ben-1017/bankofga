# Bank of Georgia

A modern digital banking system built with a microservices architecture.

## Overview

Bank of Georgia supports customer onboarding, account/product management, basic
banking transactions, and event-driven notifications. The backend is a set of
Spring Boot microservices; the frontend consists of two ReactJS applications
(customer and admin). MongoDB, Redis, and Kafka provide persistence, caching,
and event streaming, and the whole stack runs under Docker.

## Architecture

```
bank-of-georgia/
├── backend/
│   ├── api-gateway/          # Edge routing, auth
│   ├── discovery-service/    # Service registry (Eureka)
│   ├── customer-service/     # Registration, login, profile
│   ├── product-service/      # Banking product catalog
│   ├── account-service/      # Customer accounts, balances
│   ├── transaction-service/  # Deposits, withdrawals
│   ├── notification-service/ # Email/SMS via Twilio/SendGrid
│   ├── scheduler-service/    # Daily fee job
│   └── common/               # Shared DTOs, events, utils
├── frontend/
│   ├── customer-ui/          # Customer-facing React app
│   └── admin-ui/             # Employee/admin React app
├── infra/
│   ├── docker/               # docker-compose + Dockerfiles
│   ├── kafka/                # Kafka config, topic scripts
│   ├── mongo/                # Mongo init scripts
│   └── redis/                # Redis config
└── docs/                     # Design notes, API specs
```

## Core Domains

| Domain | Responsibility |
| --- | --- |
| Customer | Registration, login (hashed passwords), profile |
| Product | 5 banking products (Checking, Savings, CD, Business Checking, Student Savings) |
| Account | One customer → many accounts, balances |
| Transaction | Deposit and withdraw |
| Notification | Kafka-driven email/SMS fan-out |
| Scheduler | Daily scan → apply $5 monthly fee when daily balance < $100 on Checking |

## Kafka Events

- `APPLY_MONTHLY_FEE_EVENT` — emitted when the scheduler applies the fee
- `WITHDRAW_NOTIFICATION_EVENT` — emitted on every withdrawal
- `LOW_MAINTENANCE_NOTIFICATION_EVENT` — emitted when balance drops below threshold

## Tech Stack

- **Backend:** Java 17, Spring Boot, Maven
- **Frontend:** ReactJS
- **Data:** MongoDB, Redis
- **Events:** Apache Kafka
- **Notifications:** Twilio / SendGrid (trial tier)
- **Containers:** Docker, docker-compose

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+ and Maven (for backend development)
- Node 18+ and npm (for frontend development)

### Start the infrastructure

```bash
cd infra/docker
docker compose up -d
```

This brings up:
- MongoDB on `localhost:27017` (+ Mongo Express UI on `localhost:8081`)
- Redis on `localhost:6379`
- Zookeeper on `localhost:2181`
- Kafka on `localhost:9092` (+ Kafka UI on `localhost:8090`)

Tear it down with:

```bash
docker compose down
```

### Run a backend service (example)

```bash
cd backend/customer-service
mvn spring-boot:run
```

The customer service starts on `http://localhost:8081`. Quick smoke test:

```bash
# register
curl -X POST http://localhost:8081/api/customers/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","username":"ada","phone":"555-0100","password":"hunter2pass"}'

# login
curl -X POST http://localhost:8081/api/customers/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"ada","password":"hunter2pass"}'
```

### Run a frontend app (example)

```bash
cd frontend/customer-ui
npm install
npm run dev
```

## Non-Functional Requirements

- Passwords are hashed (BCrypt) before storage
- REST APIs follow standard conventions
- Services communicate asynchronously where possible
- Centralized exception handling per service
- Structured logging (SLF4J + Logback)

## License

Educational project — not for production use.
