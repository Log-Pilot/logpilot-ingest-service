package com.logpilot.ingest_service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record LogEventRequest(
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                message = "must be a UUID"
        )
        String eventId,

        @Size(min = 1, max = 128)
        String orgId,

        @NotNull
        @Size(min = 1, max = 128)
        String service,

        @NotNull
        @Pattern(regexp = "TRACE|DEBUG|INFO|WARN|ERROR", message = "must be TRACE, DEBUG, INFO, WARN, or ERROR")
        String level,

        @NotNull
        @Size(min = 1, max = 64)
        String timestamp,

        @NotNull
        @Size(min = 1, max = 32768)
        String message,

        @Size(max = 256)
        String traceId,

        Map<String, Object> metadata
) {
}
