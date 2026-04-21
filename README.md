# logpilot-ingest-service — Log Ingestion API

> **Receives log streams from collectors, validates payloads, and pushes them to Redis Streams for downstream processing.**

---

## 📋 Service Overview

| Property | Value |
|----------|-------|
| **Service Name** | `logpilot-ingest-service` |
| **Technology** | Spring Boot 3.x, Java 21, Redis Streams |
| **Type** | Backend Microservice |
| **Port** | `8081` |
| **Communication** | REST (inbound) → Redis Streams (outbound) |
| **Team Owner** | Backend Engineer #1 |
| **Build Priority** | Month 1, Week 3–4 |

---

## 🎯 Responsibilities

1. **Bulk Log Ingestion** — `POST /api/v1/logs/ingest` accepts single or batched log events
2. **Payload Validation** — Validate required fields (org_id, service, level, timestamp, message)
3. **Rate Limiting** — Per-tenant rate limiting (configurable, default 10,000 events/min)
4. **Collector Authentication** — Verify collector_key against registered services in PostgreSQL
5. **Redis Streams Producer** — Push validated logs to Redis Stream `logpilot:logs:{org_id}`
6. **Metrics Emission** — Track ingestion rate, validation failures, Redis write latency
7. **Service Registration** — `POST /api/v1/services/register` for onboarding new services

---

## 📦 Codebase Structure

```
logpilot-ingest-service/
├── src/
│   └── main/
│       ├── java/com/logpilot/ingest/
│       │   ├── IngestApplication.java              # Spring Boot entry point
│       │   ├── config/
│       │   │   ├── RedisConfig.java                # Redis connection + Streams setup
│       │   │   ├── SecurityConfig.java             # Collector key authentication
│       │   │   └── RateLimitConfig.java            # Per-tenant rate limits
│       │   ├── controller/
│       │   │   ├── LogIngestController.java         # POST /api/v1/logs/ingest
│       │   │   └── ServiceRegistrationController.java # POST /api/v1/services/register
│       │   ├── service/
│       │   │   ├── LogValidationService.java        # Payload validation logic
│       │   │   ├── RedisStreamProducer.java          # Write to Redis Streams
│       │   │   └── ServiceRegistryService.java       # Manage registered services
│       │   ├── model/
│       │   │   ├── LogEvent.java                    # Inbound log event DTO
│       │   │   ├── IngestResponse.java              # API response DTO
│       │   │   └── ServiceRegistration.java         # Service registration entity
│       │   ├── repository/
│       │   │   └── ServiceRepository.java           # JPA repo for services table
│       │   └── exception/
│       │       ├── ValidationException.java
│       │       └── RateLimitExceededException.java
│       └── resources/
│           ├── application.yml
│           └── logback-spring.xml
├── src/test/java/                                   # Unit & integration tests
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 🔗 Dependencies

| Dependency | Purpose |
|------------|---------|
| Spring Boot Starter Web | REST API endpoints |
| Spring Boot Starter Data Redis | Redis Streams producer |
| Spring Boot Starter Data JPA | PostgreSQL for service registry |
| Spring Boot Starter Validation | Bean validation (Jakarta) |
| Spring Boot Starter Actuator | Health checks, metrics |
| PostgreSQL Driver | JDBC driver |
| Micrometer + Prometheus | Metrics export |
| Bucket4j | Rate limiting implementation |

---

## 📡 API Endpoints

### `POST /api/v1/logs/ingest`
Bulk log ingestion — the primary entry point for all log data.

**Request:**
```json
{
  "events": [
    {
      "org_id": "acme-corp",
      "service": "payment-service",
      "level": "ERROR",
      "timestamp": "2026-04-14T10:35:22Z",
      "message": "DB connection timeout after 5000ms",
      "trace_id": "abc-123-xyz",
      "metadata": { "host": "pod-payment-7d9f", "env": "prod" }
    }
  ]
}
```

**Response:**
```json
{
  "accepted": 1,
  "rejected": 0,
  "errors": []
}
```

### `POST /api/v1/services/register`
Register a new service to receive a collector_key.

---

## 🗄️ Database Tables Owned

| Table | Purpose |
|-------|---------|
| `services` | Registered services (id, org_id, name, collector_key, created_at) |

---

## ✅ Testing Checklist

- [ ] Unit tests for LogValidationService (valid/invalid payloads)
- [ ] Unit tests for RedisStreamProducer (mock Redis)
- [ ] Integration test with embedded Redis: ingest → verify stream entry
- [ ] Rate limit test: exceed threshold → 429 response
- [ ] Load test: 10,000 events/second sustained for 5 minutes
- [ ] Authentication test: invalid collector_key → 401
