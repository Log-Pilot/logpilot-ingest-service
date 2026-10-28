package com.logpilot.ingest_service.exception;

public class RedisPublishException extends RuntimeException {
    public RedisPublishException(Throwable cause) {
        super("Events could not be accepted by the stream", cause);
    }
}
