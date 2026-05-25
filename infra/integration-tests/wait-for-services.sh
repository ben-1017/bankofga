#!/usr/bin/env bash
set -euo pipefail

# Polls each backend service over HTTP until it returns a response, then
# exits 0. Run before invoking newman so the suite starts only once every
# service is actually serving requests.
#
# A raw TCP check is NOT enough: under Docker the published port is opened
# by the proxy before the Spring Boot app inside is serving, so newman
# fires too early and hits "socket hang up". Any HTTP status code
# (200/401/404/...) proves the web layer is up and routing requests.
#
# Format: "<name>:<port>" — kept as a flat list so this works on macOS
# bash 3.2 (no associative arrays).
SERVICES=(
  "gateway:8080"
  "customer:8181"
  "product:8082"
  "account:8083"
  "transaction:8084"
  "notification:8085"
  "scheduler:8086"
)

HOST="${BOG_TEST_HOST:-localhost}"
# 6 JVMs + Kafka + Mongo cold-starting together after a fresh image build
# (e.g. CI) can take well over 90s, so default generously.
TIMEOUT_SEC="${BOG_TEST_TIMEOUT:-180}"
INTERVAL_SEC=3

wait_for_http() {
  local name=$1 port=$2 deadline=$(( $(date +%s) + TIMEOUT_SEC ))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    # "000" = no HTTP response yet (connection refused/reset/hang-up within
    # the per-probe timeout). Any other code means the app is serving.
    code=$(curl -s -o /dev/null -m 5 -w '%{http_code}' "http://${HOST}:${port}/" 2>/dev/null || true)
    if [ -n "$code" ] && [ "$code" != "000" ]; then
      echo "  ✓ ${name}-service @ ${HOST}:${port} (HTTP ${code})"
      return 0
    fi
    sleep "$INTERVAL_SEC"
  done
  echo "  ✗ ${name}-service did not serve HTTP on ${HOST}:${port} within ${TIMEOUT_SEC}s" >&2
  return 1
}

echo "Waiting for backend services (timeout: ${TIMEOUT_SEC}s/service, host: ${HOST})..."
for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  wait_for_http "$name" "$port"
done
echo "All services serving HTTP."
