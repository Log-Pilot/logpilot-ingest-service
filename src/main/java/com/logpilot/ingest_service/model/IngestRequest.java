package com.logpilot.ingest_service.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IngestRequest(
        @NotEmpty(message = "must contain at least one event")
        @Size(max = 500, message = "must contain no more than 500 events")
        List<@NotNull @Valid LogEventRequest> events
) {
}
