# Retail Backend — Cloud-Native Java/Spring Boot API (GCP Dev / GKE)

Production-style backend API for a retail platform, built to demonstrate **backend engineering + cloud-native delivery**:
- Clean Spring Boot architecture (Controller / Service / Repository)
- PostgreSQL persistence
- Kafka for async/event-driven workflows
- Redis for caching & performance
- Stripe integration for payments
- Prometheus-ready metrics (Actuator)
- Containerized with Docker
- **CI with GitHub Actions**
- **CD with Jenkins**
- Deployed to **Google Kubernetes Engine (GKE) — Dev environment**

**Author:** Takwa Suissi  
**Portfolio:** https://portfolio-showcase--suissitakwa.replit.app  
**UI Repo:** https://github.com/suissitakwa/retail-ui  
**Infra / CD Repo:** https://github.com/suissitakwa/retail-infra  

---

## Why this project

This project showcases how I build backend systems using a realistic delivery workflow:
- design APIs that are maintainable and testable
- automate builds and tests with CI
- deploy with a repeatable CD pipeline
- run on cloud infrastructure (dev environment on GCP)

> Note: The backend is currently a **modular monolith** (not microservices yet) with clear boundaries. It is designed to be evolvable as the platform grows.

---

## Tech Stack

**Backend**
- Java
- Spring Boot (Web, Data JPA, Validation, Actuator)
- REST APIs

**Data & Messaging**
- PostgreSQL
- Kafka (event-driven communication)
- Redis (caching & reduced DB load)

**Payments**
- Stripe (payment processing integration)

**DevOps & Delivery**
- Docker
- GitHub Actions (CI)
- Jenkins (CD)
- Google Cloud (GKE Dev)

**Observability**
- Prometheus (metrics collection)

**Testing**
- JUnit 5
- Mockito

---

## Architecture (High-Level)

- REST API exposes domain endpoints (e.g., products, orders, customers)
- Persistence via PostgreSQL (JPA/Hibernate)
- Redis caches hot reads and reduces load on PostgreSQL
- Kafka supports async workflows and service decoupling (current/ongoing)
- Metrics are exposed for Prometheus scraping (Actuator / Micrometer)

---

## Repository Structure (System View)

This platform is intentionally split into multiple repositories to reflect real-world separation of concerns:

- **retail** (this repo): Backend API
- **retail-ui:** Frontend application
- **retail-infra:** CD pipelines (Jenkins) + deployment automation

---

## Getting Started (Local)

### Prerequisites
- Java 17+ (or your configured version)
- Docker + Docker Compose

### Run with Docker (recommended)
```bash
git clone https://github.com/suissitakwa/retail.git
cd retail

# Build image
docker build -t retail-backend:local .

# Run container (update port if needed)
docker run --rm -p 8080:8080 retail-backend:local
