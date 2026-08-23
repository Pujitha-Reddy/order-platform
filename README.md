# Order Platform

A full event-driven e-commerce platform built from scratch: 5 Spring Boot microservices choreographed over Kafka, a gRPC-based fraud check, Redis-backed live order tracking over WebSocket, JWT authentication, and a React storefront styled after a major e-commerce site.

Built solo, end to end, as a deep dive into distributed systems patterns — not a tutorial clone. Every architectural decision, bug, and tradeoff below was hit and worked through directly.

## What it does

A customer browses a real product catalog (160 items, real photos, real prices/ratings), adds multiple items to a cart, and checks out. Behind that one click:

1. `order-service` validates the cart, computes the total server-side, and publishes `OrderCreatedEvent`
2. `inventory-service` atomically reserves stock for **every** item in the order — all-or-nothing, with rollback safety
3. `payment-service` makes a **synchronous gRPC call** to `fraud-check-service` for a real-time risk score, then "charges" the order
4. `order-service` closes the loop, marking the order `COMPLETED` or `CANCELLED`
5. `notification-service` watches every event across the whole saga and pushes live updates over WebSocket

The customer sees this happen in real time on the order tracking page — an actual event log, not a spinner.

## Architecture

- **Choreography, not orchestration** — no service directs the saga centrally; each service reacts independently to events it cares about.
- **PostgreSQL** — one database per service (`order_db`, `inventory_db`, `payment_db`), never shared.
- **Kafka** — single-broker KRaft mode locally, 3 topics, 3 partitions each.
- **gRPC** — the one synchronous call in an otherwise fully async system.
- **Redis** — TTL-based live-status cache, not a system of record.

Flow: `order-service` → Kafka `order.events` → `inventory-service` → Kafka `inventory.events` → `payment-service` (⇄ gRPC → `fraud-check-service`) → Kafka `payment.events` → back to `order-service`. `notification-service` listens to all three topics in parallel and pushes to the frontend over WebSocket.

## Stack

| Layer | Tech |
|---|---|
| Services | Spring Boot 4, Java 21 |
| Messaging | Apache Kafka (KRaft) |
| RPC | gRPC + Protocol Buffers |
| Databases | PostgreSQL (per-service), Redis |
| Auth | JWT (hand-rolled, BCrypt) |
| Frontend | React 19 + Vite, React Router, Axios |
| Infra | Docker, multi-stage Dockerfiles, Docker Compose |

## Running it locally

Requires Docker Desktop and Node.js.

```bash
git clone https://github.com/Pujitha-Reddy/order-platform.git
cd order-platform
./scripts/setup.sh
```

This builds all 5 service images, starts Kafka/Postgres/Redis/all services, waits for the schema to be ready, and seeds 160 real products. Takes a few minutes on first run.

Then, separately:
```bash
cd storefront
npm install
npm run dev
```

- Storefront: http://localhost:5173
- Ops dashboard: http://localhost:8085
- Kafka UI: http://localhost:8090
- Health check: http://localhost:8081/actuator/health

## What this project demonstrates

- Designing and implementing a choreographed saga with real compensating logic on the failure path
- Atomic multi-item operations across a Kafka event boundary (two-pass validate-then-reserve, backed by `@Transactional` + optimistic locking)
- Debugging real distributed-systems failures from raw logs: a single-broker Kafka replication-factor misconfiguration that silently hung every consumer group, cross-service Kafka type-header mismatches, Alpine/glibc native-binary incompatibilities in Docker builds, and more
- gRPC service + client design with Protocol Buffers, including the specific quirks of Spring's official gRPC starter
- JWT auth built from primitives rather than a framework, with an explicit, documented tradeoff analysis of that choice
- Full containerization with multi-stage builds and inter-service Docker networking

## Known limitations

Documented honestly rather than hidden:

- **Client-supplied unit prices** — `order-service` trusts prices sent by the client rather than validating against `inventory-service`'s catalog. A real system would never do this; it's flagged here as a deliberate scope boundary, not an oversight.
- **No compensating transaction for post-reservation payment failure** — if payment fails after inventory was reserved, that stock is never released back. `inventory-service` would need to also listen to `payment.events` to fully close this gap.
- **Hardcoded JWT secret** in `application.yml` — must be an environment variable or secrets-manager value in any real deployment.
- **No API gateway** — the frontend calls each backend service's port directly, exposing real service boundaries instead of hiding them behind a single entry point.
- **Product images** are sourced by automated keyword search against a stock-photo API; a portion of the auto-generated catalog (~160 products) have imperfect or generic matches, particularly for brand-heavy product names.
- **"Free delivery" / delivery-date copy** on the storefront is presentational only — there is no logistics or shipping system.
- **Not deployed live** — the full stack requires an always-on server for Kafka specifically; no free-tier cloud service offers this. Deployment was pursued (Oracle Cloud's Always Free ARM tier) and blocked by account-level region/capacity limits, not a technical blocker in the code itself. The system is fully container-ready (`docker-compose.full.yml`) for whenever that's resolved.
- **Cart state is in-memory only** on the frontend — resets on page refresh, not persisted server-side.

## Project structure

```
order-platform/
├── order-service/          # REST API, orders, auth, saga closure
├── inventory-service/      # Stock reservation, product catalog
├── fraud-check-service/    # gRPC fraud-check server
├── payment-service/        # gRPC client, payment processing
├── notification-service/   # WebSocket + Redis live status
├── storefront/              # React customer-facing app
├── scripts/setup.sh         # One-command local bring-up
└── docker-compose.full.yml  # Full containerized stack
```