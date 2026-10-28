package com.logpilot.ingest_service.model;

import java.util.List;

public record IngestResponse(int accepted, int rejected, List<IngestError> errors) {
}
