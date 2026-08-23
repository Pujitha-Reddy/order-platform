#!/bin/bash
set -e

cd "$(dirname "$0")/.."
ROOT=$(pwd)

echo "== Order Platform — Local Setup =="

if ! docker info > /dev/null 2>&1; then
  echo "ERROR: Docker is not running. Start Docker Desktop and try again."
  exit 1
fi

echo ""
echo "-- Building service images --"
for svc in order-service inventory-service fraud-check-service payment-service notification-service; do
  echo "  Building $svc..."
  docker build -t "$svc:local" "$ROOT/$svc" > /dev/null
done

echo ""
echo "-- Starting infrastructure + services (fresh state) --"
docker compose -f "$ROOT/docker-compose.full.yml" down -v > /dev/null 2>&1 || true
docker compose -f "$ROOT/docker-compose.full.yml" up -d

echo ""
echo "-- Waiting for Postgres to accept connections --"
until docker exec order-platform-postgres pg_isready -U platform > /dev/null 2>&1; do
  sleep 2
done

echo "-- Waiting for inventory-service to create its schema --"
for i in $(seq 1 30); do
  if docker exec order-platform-postgres psql -U platform -d inventory_db -tAc \
      "SELECT to_regclass('public.products');" 2>/dev/null | grep -q products; then
    break
  fi
  sleep 2
done

echo ""
echo "-- Seeding product catalog --"
docker exec -i order-platform-postgres psql -U platform -d inventory_db < "$ROOT/scripts/seed/products.sql" > /dev/null

echo ""
echo "-- Verifying --"
COUNT=$(docker exec order-platform-postgres psql -U platform -d inventory_db -tAc "SELECT count(*) FROM products;")
echo "  Products seeded: $COUNT"

echo ""
echo "== Setup complete =="
echo "  Storefront (run separately): cd storefront && npm install && npm run dev  ->  http://localhost:5173"
echo "  Ops dashboard:  http://localhost:8085"
echo "  Kafka UI:       http://localhost:8090"
echo "  order-service:  http://localhost:8081/actuator/health"