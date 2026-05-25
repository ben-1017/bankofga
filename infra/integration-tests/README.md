# Integration tests

Newman-based end-to-end runner that exercises the Docker stack through the API gateway via `docs/BankOfGA-Sprint4.postman_collection.json`.

## What it does

`wait-for-services.sh` polls HTTP on each backend service port until reachable, then `newman` runs every request in order. Variables (`customerId`, `accountId`, `transactionId`, ...) are chained between requests by the collection's pre-request and test scripts. A failure in any request fails the whole run.

## Usage

```bash
# 1. From repo root, bring up the stack (or run individual services with mvn)
docker compose -f infra/docker/docker-compose.yml up -d

# 2. Install runner deps once
cd infra/integration-tests
npm install

# 3. Wait for all services to be ready, then run the suite
npm run ci
```

`npm test` skips the wait step. Reports land in `infra/integration-tests/reports/junit.xml`.

## Tuning

Environment variables read by `wait-for-services.sh`:

- `BOG_TEST_HOST` — defaults to `localhost`. Set to `host.docker.internal` or a service name when running newman from inside the docker network.
- `BOG_TEST_TIMEOUT` — total wait timeout in seconds, default `90`.

## CI

Designed to run after `docker compose up -d` in a workflow:

```yaml
- run: docker compose -f infra/docker/docker-compose.yml up -d --build
- run: cd infra/integration-tests && npm ci && npm run ci
```

A GitHub Actions wiring can use the same two commands above.
