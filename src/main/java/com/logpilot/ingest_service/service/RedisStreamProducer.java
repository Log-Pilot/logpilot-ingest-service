package com.logpilot.ingest_service.service;

import com.logpilot.ingest_service.exception.RedisPublishException;
import com.logpilot.ingest_service.model.LogEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisStreamProducer {
    public static final String STREAM_KEY = "logpilot:logs";

    private static final String PUBLISH_BATCH_SCRIPT = """
            for i = 1, #ARGV do
              redis.call('XADD', KEYS[1], '*', 'event', ARGV[i])
            end
            return #ARGV
            """;

    private final StringRedisTemplate redisTemplate;
    private final tools.jackson.databind.ObjectMapper objectMapper;
    private final DefaultRedisScript<Long> publishBatchScript =
            new DefaultRedisScript<>(PUBLISH_BATCH_SCRIPT, Long.class);

    public RedisStreamProducer(
            StringRedisTemplate redisTemplate,
            tools.jackson.databind.ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishBatch(List<LogEvent> events) {
        List<String> payloads = events.stream()
                .map(this::serialize)
                .toList();
        try {
            Long published = redisTemplate.execute(
                    publishBatchScript,
                    List.of(STREAM_KEY),
                    payloads.toArray()
            );
            if (published == null || published != payloads.size()) {
                throw new RedisPublishException(
                        new IllegalStateException("Redis did not confirm the complete batch")
                );
            }
        } catch (DataAccessException exception) {
            throw new RedisPublishException(exception);
        }
    }

    private String serialize(LogEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (tools.jackson.core.JacksonException exception) {
            throw new IllegalStateException("Event could not be serialized", exception);
        }
    }
}
