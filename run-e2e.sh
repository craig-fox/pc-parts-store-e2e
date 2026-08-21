#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$SCRIPT_DIR/../pc-parts-store-api"

wait_for_service() {
    local name="$1"
    local url="$2"
    local max_attempts=30
    local attempt=1

    echo "Waiting for $name..."

    while ! curl --silent --fail "$url" > /dev/null; do
        if (( attempt >= max_attempts )); then
            echo "ERROR: $name did not become ready."
            return 1
        fi

        sleep 2
        attempt=$((attempt + 1))
    done

    echo "$name is ready."
}

echo "Building API services..."
(
    cd "$API_DIR"
    mvn clean package -DskipTests
)

echo "Resetting E2E environment..."
(
    cd "$API_DIR"
    docker compose \
        -f docker-compose.yml \
        -f docker-compose.e2e.yml \
        down -v
)

echo "Starting E2E environment..."
(
    cd "$API_DIR"
    docker compose \
        -f docker-compose.yml \
        -f docker-compose.e2e.yml \
        up --build -d
)

wait_for_service "customer-service" "http://localhost:8081/actuator/health"
wait_for_service "product-service" "http://localhost:8083/actuator/health"
wait_for_service "order-service" "http://localhost:8082/actuator/health"
wait_for_service "inventory-service" "http://localhost:8084/actuator/health"
wait_for_service "authentication-service" "http://localhost:8085/actuator/health"

echo "Container JWT configuration:"

docker compose \
    -f "$API_DIR/docker-compose.yml" \
    -f "$API_DIR/docker-compose.e2e.yml" \
    exec -T authentication-service \
    sh -c 'echo "authentication-service JWT_SECRET length: ${#JWT_SECRET}"'

docker compose \
    -f "$API_DIR/docker-compose.yml" \
    -f "$API_DIR/docker-compose.e2e.yml" \
    exec -T order-service \
    sh -c 'echo "order-service JWT_SECRET length: ${#JWT_SECRET}"'

echo "All services are ready."
echo "Running Cucumber tests..."

mvn test

echo "E2E tests passed successfully."