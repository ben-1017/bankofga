# Bank of Georgia

A modern digital banking system built with a microservices architecture.

## Overview

Bank of Georgia supports customer onboarding, account/product management, basic
banking transactions, and event-driven notifications. The backend is a set of
Spring Boot microservices; the frontend consists of two ReactJS applications
(customer and admin). MongoDB and Kafka provide persistence and event
streaming. Redis is provisioned by Docker for future caching experiments, but
the services do not currently require it.

## Architecture

```
bank-of-georgia/
├── backend/
│   ├── api-gateway/          # Edge routing, auth
│   ├── customer-service/     # Registration, login, profile
│   ├── product-service/      # Banking product catalog
│   ├── account-service/      # Customer accounts, balances
│   ├── transaction-service/  # Deposits, withdrawals
│   ├── notification-service/ # Email/SMS via Twilio/SendGrid
│   └── scheduler-service/    # Daily fee job
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
| Scheduler | Daily scan → apply $5 monthly fee to low-balance Checking accounts and notify by email |

## Kafka Events

- `APPLY_MONTHLY_FEE_EVENT` — emitted when the scheduler applies the fee
- `WITHDRAW_NOTIFICATION_EVENT` — emitted on every withdrawal
- `LOW_MAINTENANCE_NOTIFICATION_EVENT` — emitted when balance drops below threshold

## Tech Stack

- **Backend:** Java 17, Spring Boot, Maven
- **Frontend:** ReactJS
- **Data:** MongoDB (Redis is provisioned but currently optional)
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
- Redis on `localhost:6379` (optional; not used by services yet)
- Zookeeper on `localhost:2181`
- Kafka on `localhost:9092` (+ Kafka UI on `localhost:8090`)

### Optional notification provider credentials

The notification service persists every notification even without provider
credentials. Real email/SMS delivery is best-effort and starts only when the
provider environment variables are set:

```bash
cd infra/docker
cp .env.example .env
# edit .env with a verified SENDGRID_FROM and a real SENDGRID_API_KEY
docker compose up -d --build notification-service
```

For email, configure `SENDGRID_API_KEY` and `SENDGRID_FROM`. For SMS, configure
`TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, and `TWILIO_FROM_NUMBER`.

Tear it down with:

```bash
docker compose down
```

### Run a backend service (example)

```bash
cd backend/customer-service
mvn spring-boot:run
```

The customer service starts on `http://localhost:8181` (port 8081 is taken by
Mongo Express). Quick smoke test:

```bash
# register
curl -X POST http://localhost:8181/api/customers/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","username":"ada","phone":"555-0100","password":"hunter2pass"}'

# login
curl -X POST http://localhost:8181/api/customers/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"ada","password":"hunter2pass"}'
```

### Run a frontend app (example)

```bash
cd frontend/customer-ui
npm install
npm run dev
```

## User Testing (Sprint 4) — APIs via Docker + Postman

For another team to test the system end-to-end. Everything runs as Docker
containers behind the API gateway; all calls are JWT-authenticated.

**1. Get the latest code**

```bash
git checkout development && git pull
```

**2. Bring the whole backend up as containers**

```bash
cd infra/docker
docker compose up -d --build      # builds + starts all 7 services + Mongo/Kafka/Redis
```

Give it ~1–2 min. Readiness check (any HTTP code = serving):

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/products   # 200 when ready
```

**3. Drive the APIs — Postman collection**

Import **`docs/BankOfGA-Sprint4.postman_collection.json`** into Postman and
**Run collection** (or use the CLI below). It is rerunnable — each run
registers a fresh customer via a timestamp — and covers, through the gateway
(`http://localhost:8080`) with JWT:

- Auth (customer register/login, employee login — tokens captured automatically)
- Products (employee CRUD, incl. the duplicate-rejected 409 check)
- Accounts (open, get, list-by-customer)
- Transactions (deposit, withdraw, history; one withdrawal trips the low-balance alert)
- Notifications (Kafka-driven; arrive asynchronously)
- Security negatives (no-token → 401, customer-list-all → 403, `/internal/` blocked)

CLI alternative (no Postman UI needed):

```bash
./infra/integration-tests/node_modules/.bin/newman run docs/BankOfGA-Sprint4.postman_collection.json
```

Seeded employee logins: `admin@bankofga.com / admin123`,
`quincy@bankofga.com / demo123`, `ben@bankofga.com / demo123`.

**4. Optional — the web UIs** (run as Vite dev servers, not containers)

```bash
cd frontend/admin-ui    && npm install && npm run dev   # http://localhost:3001  (employee login)
cd frontend/customer-ui && npm install && npm run dev   # http://localhost:3000  (register a customer)
```

## Final Demo Script

Use this path for a concise end-to-end project demo.

### Customer UI (`http://localhost:3000`)

1. Register a new customer and log in.
2. Open a Checking account.
3. Deposit enough to put the balance above `$100`.
4. Withdraw enough to cross below `$100`.
5. Confirm transaction history shows the deposit and withdrawal.
6. Confirm notifications show both withdrawal confirmation and low-balance alert.
7. Update the customer profile.

### Admin UI (`http://localhost:3001`)

1. Log in as `admin@bankofga.com / admin123`.
2. Create or edit a banking product.
3. View the customer list and open the newly registered customer.
4. View the account list and open the customer's account detail.
5. Confirm account balance, product, customer metadata, and transaction history are visible.

### Scheduled Monthly Fee

The scheduler runs from `scheduler-service` with `app.fee.cron: "0 0 1 * * *"`.
It scans low-balance accounts, charges the `$5` fee only for Checking products,
records a `FEE` transaction through transaction-service, and publishes the fee
notification event.

### Delivery Credentials and OTP Scope

In-app notifications work without external credentials. Real email delivery
requires SendGrid credentials; real SMS delivery requires Twilio credentials.
OTP/2FA is not implemented in this project: provider credentials would only
deliver an OTP after a separate OTP generation, storage, expiry, and
verification flow is added.

**Tear down:** `cd infra/docker && docker compose down` (add `-v` to also wipe data).

## Non-Functional Requirements

- Passwords are hashed (BCrypt) before storage
- REST APIs follow standard conventions
- Services communicate asynchronously where possible
- Centralized exception handling per service
- Structured logging (SLF4J + Logback)

## License

Educational project — not for production use.
