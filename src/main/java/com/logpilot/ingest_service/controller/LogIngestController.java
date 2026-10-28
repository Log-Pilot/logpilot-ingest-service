package com.logpilot.ingest_service.controller;

import com.logpilot.ingest_service.model.IngestRequest;
import com.logpilot.ingest_service.model.IngestResponse;
import com.logpilot.ingest_service.service.LogIngestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
public class LogIngestController {
    private final LogIngestService ingestService;

    public LogIngestController(LogIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponse> ingest(@Valid @RequestBody IngestRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ingestService.ingest(request));
    }
}
