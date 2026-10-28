package com.logpilot.ingest_service.exception;

import com.logpilot.ingest_service.model.IngestError;

import java.util.List;

public class InvalidEventException extends RuntimeException {
    private final List<IngestError> errors;

    public InvalidEventException(List<IngestError> errors) {
        super("One or more events are invalid");
        this.errors = List.copyOf(errors);
    }

    public List<IngestError> getErrors() {
        return errors;
    }
}
