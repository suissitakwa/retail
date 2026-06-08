# Retail Backend — Spring Boot 3.5 / Java 17

[![CI](https://github.com/suissitakwa/retail/actions/workflows/build.yml/badge.svg)](https://github.com/suissitakwa/retail/actions/workflows/build.yml)

Production-style e-commerce backend built to demonstrate backend engineering, cloud-native delivery, and AI integration. Deployed to **Google Kubernetes Engine (GKE)** via a Jenkins CD pipeline.

**Frontend:** https://github.com/suissitakwa/retail-ui  
**Microservices layer:** https://github.com/suissitakwa/retail-microservices  
**Infrastructure / CD:** https://github.com/suissitakwa/retail-infra  
**Portfolio:** https://portfolio-showcase--suissitakwa.replit.app

---

## Tech Stack

| Concern | Technology |
|---|---|
| Runtime | Java 17, Spring Boot 3.5 |
| Persistence | PostgreSQL + Spring Data JPA + Flyway (migrations V1–V5) |
| Caching | Redis via Spring Cache (`spring.cache.type=redis`) |
| Messaging | Apache Kafka — async order + payment event flow |
| Payments | Stripe (session checkout + webhook-driven confirmation) |
| Auth | JWT (HS256) — access token 2.4 h, refresh token 7 d |
| AI | OpenAI GPT-4o-mini — backend-controlled facts, no direct DB access |
| Observability | Prometheus metrics via Spring Boot Actuator + Micrometer |
| Containerisation | Docker (multi-stage build) |
| CI | GitHub Actions — build, test, Docker image push |
| CD | Jenkins → GKE (`retail-dev` namespace) |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                    REST API  :8080                                │
│  /auth/**  /api/v1/products  /api/v1/orders  /api/v1/cart  …    │
└────────────────────────┬─────────────────────────────────────────┘
                         │ JwtAuthenticationFilter (stateless)
              ┌──────────┴──────────┐
              │   Spring Security   │
              │  ROLE_CUSTOMER      │
              │  ROLE_ADMIN         │
              └──────────┬──────────┘
       ┌──────────────────┼──────────────────┐
       ▼                  ▼                  ▼
  PostgreSQL           Redis              Kafka
  (Flyway DDL)      (hot reads)     order.created
                                    payment.processed
```

**Package layout** — flat package-per-domain under `com.retail_project`:

| Package | Responsibility |
|---|---|
| `auth/` | register, login, refresh, forgot/reset-password |
| `product/` `category/` `inventory/` | product catalogue + stock management |
| `cart/` `cartItem/` | session cart (persisted per customer) |
| `order/` `orderItem/` | checkout → Stripe session → order lifecycle |
| `payment/` | Stripe webhook handling; PENDING → PAID → Kafka event |
| `notification/` | ORDER_PLACED + PAYMENT_PAID notifications saved via Kafka consumer |
| `customer/` | profiles, admin CRUD |
| `copilot/` | OpenAI integration — LLM receives pre-built facts string, never queries DB |
| `Kafka/` | `KafkaConfig`, producers, consumers |
| `config/jwt/` | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService` |

---

## Kafka Event Flow

```
POST /api/v1/stripe/checkout
  → createOrderFromCart()
  → OrderProducer  ──► order.created
                            ├── OrderConsumer: decrement inventory
                            └── OrderConsumer: save ORDER_PLACED notification

Stripe webhook: payment_intent.succeeded
  → PaymentService.markPaymentAsPaidByIntent()
  → PaymentProducer ──► payment.processed
                             └── PaymentConsumer: save PAYMENT_PAID notification
```

`KAFKA_ENABLED=false` disables listener auto-startup — used in GKE dev and integration tests.

---

## Stripe Flow

1. `POST /api/v1/stripe/checkout` → creates Stripe Session, saves `PENDING` Payment row
2. Webhook `checkout.session.completed` → attaches `paymentIntentId` to Payment row
3. Webhook `payment_intent.succeeded` → marks Payment `PAID`, Order `COMPLETED`, publishes Kafka event

---

## AI Copilot

The copilot answers customer order questions via GPT-4o-mini. The design principle:

> **The backend owns correctness. The LLM owns language.**

- JWT auth + ownership check happen before any LLM call
- Backend fetches verified order data and builds a structured "facts string"
- Only the facts string is sent to OpenAI — the LLM never touches the database
- Returns `{ answer, actions }` where `actions` drives UI navigation (e.g. `OPEN_ORDER_DETAILS`)

---

## Getting Started

### Prerequisites
- Java 17+, Docker + Docker Compose
- A `.env` file in `retail/` (see below)

### Run locally

```bash
git clone https://github.com/suissitakwa/retail.git
cd retail

# Start infra (Postgres + Redis + Kafka)
docker-compose up -d retail-db redis kafka

# Run the app
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

### Required `.env`

```
STRIPE_SECRET_KEY=...
STRIPE_WEBHOOK_SECRET=...
JWT_SECRET_KEY=...
DB_USERNAME=user
DB_PASSWORD=password
FLYWAY_URL=jdbc:postgresql://localhost:5433/retail_db
```

### Run tests

```bash
./mvnw test           # unit tests (WebMvcTest slices — no Docker needed)
./mvnw verify         # unit + integration tests (Testcontainers — requires Docker)
./mvnw test -Dtest=OrderControllerTest   # single class
```

---

## Testing Strategy

| Type | Annotation | What it covers |
|---|---|---|
| Unit (`*Test.java`) | `@WebMvcTest` | Controller layer — services mocked with `@MockitoBean` |
| Integration (`*IT.java`) | `@SpringBootTest` + Testcontainers | Full stack against a real PostgreSQL container; Flyway runs real migrations |

`SecurityAutoConfiguration` is excluded in all `@WebMvcTest` classes. `JwtService` and `CustomerRepository` are mocked in every slice test because `JwtAuthenticationFilter` is a `@Component` Filter picked up by the slice.

---

## CI / CD

```
git push → GitHub Actions (build.yml)
             ├── compile + unit tests
             ├── integration tests (Testcontainers)
             ├── docker build + push to registry
             └── LLM PR diff summarizer (llm-pr-summary.yml)
                      ↓
           Jenkins (retail-infra/Jenkinsfile)
             ├── apply k8s manifests (namespace, secrets, postgres, redis)
             ├── deploy backend + UI
             └── rollout status check
```

---

## Platform Overview

This monolith is part of a four-repository retail platform. The microservices layer (`retail-microservices`) runs alongside it — business services progressively delegate to the microservices as they mature.

| Repo | Purpose |
|---|---|
| **retail** (this) | Spring Boot 3.5 monolith — primary API |
| [retail-ui](https://github.com/suissitakwa/retail-ui) | React 19 frontend — Novamart dark theme |
| [retail-microservices](https://github.com/suissitakwa/retail-microservices) | Spring Boot 4 / Java 21 — 8-service microservices |
| [retail-infra](https://github.com/suissitakwa/retail-infra) | Jenkins CD + GKE k8s manifests |

---

**Author:** Takwa Suissi  
**Portfolio:** https://portfolio-showcase--suissitakwa.replit.app
