package com.logpilot.ingest_service.model;

import java.util.List;

public record ApiErrorResponse(String code, String message, List<IngestError> errors) {
}
