# Retail Backend — Cloud-Native Java/Spring Boot API (GCP Dev / GKE)

Production-style backend API for a retail platform, built to demonstrate **backend engineering + cloud-native delivery, and production-grade AI integration**:
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
- AI-powered customer support (LLM Copilot)



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


  **AI / LLM**
- OpenAI API (LLM Copilot with backend-controlled facts)
- 🤖 CI/CD AI Assistant: LLM-based PR Diff Summarizer (auto-generates change summary + risk checklist in GitHub Actions)


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
```
---

## 🤖 AI / LLM Integration — Retail Support Copilot

This project includes an **AI-powered Retail Support Copilot** designed using
**production-grade LLM integration principles**.

### What This Demonstrates

- How to integrate Large Language Models into an existing backend safely
- How to prevent hallucinations by keeping the backend as the source of truth
- How to combine AI with authentication, authorization, and domain logic

### Core Design Principle

> **The backend owns correctness.  
> The LLM owns language.**

### High-Level Flow

1. Customer sends a natural-language support question
2. JWT authentication & authorization are enforced by Spring Security
3. Backend services validate ownership and fetch verified data
4. Only verified facts are passed to the LLM
5. The LLM rewrites the response in a friendly, human tone
6. Backend returns a structured, auditable response

### Security & Reliability Guarantees

- LLM never accesses the database
- LLM never executes business logic
- LLM never bypasses authorization
- Backend remains deterministic and testable

This mirrors how AI copilots are implemented in **real production systems**.

➡️ **LLM implementation & demo (Gradio UI):**  
`/llm` directory in this repository

### CI/CD LLM Automation
- Pull Request Diff Summarizer using LLMs for engineering visibility and risk awareness
