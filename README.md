# LogPilot Ingest Service

Spring Boot API for validating batches of LogPilot events and publishing them to the local Redis Streams queue.

## Implemented in the first iteration

- `POST /api/v1/logs/ingest` accepts a JSON object with an `events` array.
- Batch size is limited to 1–500 events.
- Required fields: `service`, `level`, `timestamp`, and `message`.
- Supported levels: `TRACE`, `DEBUG`, `INFO`, `WARN`, and `ERROR`.
- Timestamps must be ISO-8601 date-times with an offset and are normalized to UTC.
- `event_id` is an optional UUID. A missing ID is generated at ingestion time; clients should send stable IDs if they may retry.
- `org_id` defaults to `demo`. This is a local demo value, not tenant isolation.
- Unknown JSON properties are rejected.
- Validation is atomic: an invalid event rejects the complete request.
- Valid events are serialized as JSON in the `event` field of Redis stream `logpilot:logs`.
- A successful response is HTTP 202 and means Redis accepted the events. It does not mean the processor has persisted them.
- If Redis is unavailable, the API returns HTTP 503.

## Run locally

Requirements: Java 21 and Redis. The LogPilot infrastructure repository provides local Redis/PostgreSQL Compose setup. From `logpilot-infra`, run `docker compose up -d redis`; then from this repository:

```sh
sh ./mvnw spring-boot:run
```

The service listens on port 8081 by default. Configure `REDIS_HOST` and `REDIS_PORT` to override the default `localhost:6379`.

## Request example

```sh
curl --fail-with-body -X POST http://localhost:8081/api/v1/logs/ingest \
  -H 'Content-Type: application/json' \
  -d '{
    "events": [
      {
        "event_id": "0192f6e4-7d7a-7b03-a909-443f6f27c853",
        "org_id": "demo",
        "service": "checkout-api",
        "level": "ERROR",
        "timestamp": "2026-10-28T10:35:22.123Z",
        "message": "Database connection timed out",
        "trace_id": "trace-demo-001",
        "metadata": {
          "environment": "local",
          "host": "developer-machine"
        }
      }
    ]
  }'
```

Successful response:

```json
{
  "accepted": 1,
  "rejected": 0,
  "errors": []
}
```

Validation errors return HTTP 400 with an `errors` list containing the batch index, field, and message. Redis errors return HTTP 503. Do not send secrets or sensitive production logs to this unauthenticated local-demo endpoint.

## Not implemented yet

Collector integrations, service registration/authentication, tenant isolation, rate limiting, PostgreSQL service registry, metrics/Actuator, Docker deployment, production request-size controls, and load testing remain future work. The README from the original project described a larger design; this section reflects the current code.
