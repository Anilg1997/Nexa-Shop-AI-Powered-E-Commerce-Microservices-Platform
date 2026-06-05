package com.anilg.ecommerce.analytics;

import com.anilg.ecommerce.common.DomainEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsListener {
    private final EventRecordRepository events;

    public AnalyticsListener(EventRecordRepository events) {
        this.events = events;
    }

    @KafkaListener(topics = "commerce.events", groupId = "analytics-service")
    public void onEvent(DomainEvent event) {
        EventRecord record = new EventRecord();
        record.setEventId(event.eventId());
        record.setType(event.type());
        record.setAggregateId(event.aggregateId());
        record.setActor(event.actor());
        record.setPayload(String.valueOf(event.payload()));
        record.setOccurredAt(event.occurredAt());
        events.save(record);
    }
}
