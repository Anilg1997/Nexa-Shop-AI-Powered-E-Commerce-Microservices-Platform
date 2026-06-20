package com.anilg.ecommerce.notification;

import com.anilg.ecommerce.common.DomainEvent;
import java.util.Set;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EventNotificationListener {
    private static final Set<String> NOTIFY_EVENTS = Set.of(
            "USER_REGISTERED", "ORDER_CREATED", "PAYMENT_COMPLETED", "ORDER_SHIPPED", "ORDER_DELIVERED"
    );
    private final NotificationRepository notifications;

    public EventNotificationListener(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @KafkaListener(topics = "commerce.events", groupId = "notification-service")
    public void onEvent(DomainEvent event) {
        if (!NOTIFY_EVENTS.contains(event.type())) {
            return;
        }
        NotificationMessage message = new NotificationMessage();
        message.setCustomerEmail(event.actor());
        message.setChannel("EMAIL");
        message.setSubject("Nexa Shop update: " + event.type());
        message.setBody("Demo email generated for event " + event.type() + " with payload " + event.payload());
        message.setStatus("SENT_DEMO");
        notifications.save(message);
    }
}
