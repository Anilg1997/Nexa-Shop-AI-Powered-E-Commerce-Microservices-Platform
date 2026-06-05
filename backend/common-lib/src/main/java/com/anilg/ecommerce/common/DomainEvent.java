package com.anilg.ecommerce.common;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventId,
        String type,
        String aggregateId,
        String actor,
        Map<String, Object> payload,
        Instant occurredAt
) {
    public static DomainEvent of(String type, String aggregateId, String actor, Map<String, Object> payload) {
        return new DomainEvent(UUID.randomUUID().toString(), type, aggregateId, actor, payload, Instant.now());
    }
}
