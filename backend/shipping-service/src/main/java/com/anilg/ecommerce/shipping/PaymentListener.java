package com.anilg.ecommerce.shipping;

import com.anilg.ecommerce.common.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentListener {
    private final ShipmentRepository shipments;
    private final KafkaTemplate<String, DomainEvent> kafka;

    public PaymentListener(ShipmentRepository shipments, KafkaTemplate<String, DomainEvent> kafka) {
        this.shipments = shipments;
        this.kafka = kafka;
    }

    @KafkaListener(topics = "commerce.events", groupId = "shipping-service")
    public void onEvent(DomainEvent event) {
        if (!"PAYMENT_COMPLETED".equals(event.type())) {
            return;
        }
        Long orderId = Long.valueOf(String.valueOf(event.payload().get("orderId")));
        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setCustomerEmail(event.actor());
        shipment.setAddress("Demo shipping address");
        shipment.setStatus("SHIPPED");
        shipment.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        shipment.setUpdatedAt(Instant.now());
        Shipment saved = shipments.save(shipment);
        kafka.send("commerce.events", String.valueOf(orderId), DomainEvent.of(
                "ORDER_SHIPPED",
                String.valueOf(orderId),
                saved.getCustomerEmail(),
                Map.of("orderId", orderId, "trackingNumber", saved.getTrackingNumber())
        ));
    }
}
