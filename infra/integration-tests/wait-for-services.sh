#!/usr/bin/env bash
set -euo pipefail

# Polls each backend service's HTTP port until reachable, then exits 0.
# Run before invoking newman so the test suite starts only when every
# service is actually serving requests.
#
# Format: "<name>:<port>" — kept as a flat list so this works on macOS
# bash 3.2 (no associative arrays).
SERVICES=(
  "customer:8181"
  "product:8082"
  "account:8083"
  "transaction:8084"
  "notification:8085"
)

HOST="${BOG_TEST_HOST:-localhost}"
TIMEOUT_SEC="${BOG_TEST_TIMEOUT:-90}"
INTERVAL_SEC=2

wait_for_port() {
  local name=$1 port=$2 deadline=$(( $(date +%s) + TIMEOUT_SEC ))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    if (echo > "/dev/tcp/${HOST}/${port}") 2>/dev/null; then
      echo "  ✓ ${name}-service @ ${HOST}:${port}"
      return 0
    fi
    sleep "$INTERVAL_SEC"
  done
  echo "  ✗ ${name}-service did not come up on ${HOST}:${port} within ${TIMEOUT_SEC}s" >&2
  return 1
}

echo "Waiting for backend services (timeout: ${TIMEOUT_SEC}s, host: ${HOST})..."
for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  wait_for_port "$name" "$port"
done
echo "All services reachable."
