package com.logpilot.ingest_service.model;

public record IngestError(Integer eventIndex, String field, String message) {
}
