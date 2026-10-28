package com.logpilot.ingest_service.service;

import com.logpilot.ingest_service.exception.InvalidEventException;
import com.logpilot.ingest_service.model.IngestError;
import com.logpilot.ingest_service.model.LogEvent;
import com.logpilot.ingest_service.model.LogEventRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class LogEventNormalizer {
    public List<LogEvent> normalize(List<LogEventRequest> requests) {
        List<LogEvent> normalized = new ArrayList<>(requests.size());
        List<IngestError> errors = new ArrayList<>();

        for (int index = 0; index < requests.size(); index++) {
            LogEventRequest request = requests.get(index);
            UUID eventId = parseEventId(request.eventId(), index, errors);
            var timestamp = parseTimestamp(request.timestamp(), index, errors);
            if (eventId == null || timestamp == null) {
                continue;
            }

            Map<String, Object> metadata = request.metadata() == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(request.metadata()));

            normalized.add(new LogEvent(
                    eventId,
                    request.orgId() == null ? "demo" : request.orgId(),
                    request.service(),
                    request.level().toUpperCase(Locale.ROOT),
                    timestamp,
                    request.message(),
                    request.traceId(),
                    metadata
            ));
        }

        if (!errors.isEmpty()) {
            throw new InvalidEventException(errors);
        }
        return List.copyOf(normalized);
    }

    private UUID parseEventId(String value, int index, List<IngestError> errors) {
        if (value == null) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            errors.add(new IngestError(index, "event_id", "must be a valid UUID"));
            return null;
        }
    }

    private java.time.Instant parseTimestamp(String value, int index, List<IngestError> errors) {
        if (value == null) {
            errors.add(new IngestError(index, "timestamp", "must be an ISO-8601 date-time with a timezone"));
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException exception) {
            errors.add(new IngestError(index, "timestamp", "must be an ISO-8601 date-time with a timezone"));
            return null;
        }
    }
}
