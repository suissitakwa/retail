# Retail Backend — Cloud-Native Java/Spring Boot API (GCP Dev)

Production-style backend API for a retail platform, built to demonstrate **backend engineering + cloud-native delivery**:
- Clean Spring Boot architecture (Controller/Service/Repository)
- PostgreSQL persistence
- Containerized with Docker
- **CI with GitHub Actions**
- **CD with Jenkins**
- Deployed to **Google Cloud (Dev environment)**

**Author:** Takwa Suissi  
**Portfolio:** https://portfolio-showcase--suissitakwa.replit.app  
**UI Repo:** https://github.com/suissitakwa/retail-ui  
**Infra/Delivery Repo (Jenkins/CD):** https://github.com/suissitakwa/retail-infra  

---

## Why this project

This project showcases how I build backend systems in a real-world workflow:
- design APIs that are maintainable and testable
- automate builds and tests with CI
- deploy with a repeatable CD pipeline
- run on cloud infrastructure (dev environment on GCP)

> Note: The backend is currently a **modular monolith** (not microservices yet) with clear boundaries, designed to be evolvable as the platform grows.

---

## Tech Stack

**Backend**
- Java
- Spring Boot (Web, Data JPA, Validation)
- REST APIs

**Data & Messaging**
- PostgreSQL
- Kafka (event-driven communication)
- Redis (caching, performance optimization)

**Payments**
- Stripe (payment processing integration)

**DevOps & Delivery**
- Docker
- GitHub Actions (CI)
- Jenkins (CD)
- Google Cloud (Dev environment)

**Observability**
- Prometheus (metrics collection, dev-level monitoring)

**Testing**
- JUnit 5
- Mockito
---

## Architecture (High-Level)

- REST API exposes domain endpoints (e.g., products, orders, customers)
- Persistence via PostgreSQL (JPA/Hibernate)
- Environment-based configuration (local/dev)
- Delivery workflow:
  - **CI:** GitHub Actions runs build + tests on every PR/push
  - **CD:** Jenkins deploys to Google Cloud dev (build image → push → rollout)

---

## Repository Links (System View)

This platform is split into multiple repositories to reflect separation of concerns:

- **retail** (this repo): Backend API
- **retail-ui:** Frontend application
- **retail-infra:** Delivery pipelines (Jenkins) and deployment automation

---

## Getting Started (Local)

### Prerequisites
- Java 17+ (or your configured version)
- Docker + Docker Compose
- (Optional) Maven/Gradle if not using wrapper

### Run with Docker (recommended)
```bash
git clone https://github.com/suissitakwa/retail.git
cd retail

# Build and run 
docker build -t retail-backend:local .
docker run --rm -p 8080:8080 retail-backend:local
