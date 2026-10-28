package com.logpilot.ingest_service.service;

import com.logpilot.ingest_service.model.IngestRequest;
import com.logpilot.ingest_service.model.IngestResponse;
import com.logpilot.ingest_service.model.LogEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogIngestService {
    private final LogEventNormalizer normalizer;
    private final RedisStreamProducer streamProducer;

    public LogIngestService(LogEventNormalizer normalizer, RedisStreamProducer streamProducer) {
        this.normalizer = normalizer;
        this.streamProducer = streamProducer;
    }

    public IngestResponse ingest(IngestRequest request) {
        List<LogEvent> events = normalizer.normalize(request.events());
        streamProducer.publishBatch(events);
        return new IngestResponse(events.size(), 0, List.of());
    }
}
