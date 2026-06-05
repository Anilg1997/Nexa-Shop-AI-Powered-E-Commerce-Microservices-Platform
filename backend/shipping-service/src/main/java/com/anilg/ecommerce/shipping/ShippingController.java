package com.anilg.ecommerce.shipping;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {
    private final ShipmentRepository shipments;
    private final KafkaTemplate<String, DomainEvent> kafka;

    public ShippingController(ShipmentRepository shipments, KafkaTemplate<String, DomainEvent> kafka) {
        this.shipments = shipments;
        this.kafka = kafka;
    }

    @GetMapping
    public ApiResponse<List<Shipment>> byCustomer(@RequestParam String customerEmail) {
        return ApiResponse.ok(shipments.findByCustomerEmailOrderByUpdatedAtDesc(customerEmail));
    }

    @PostMapping("/{orderId}/deliver")
    public ApiResponse<Shipment> deliver(@PathVariable Long orderId) {
        Shipment shipment = shipments.findByOrderId(orderId).orElseThrow();
        shipment.setStatus("DELIVERED");
        shipment.setUpdatedAt(Instant.now());
        Shipment saved = shipments.save(shipment);
        kafka.send("commerce.events", String.valueOf(orderId), DomainEvent.of(
                "ORDER_DELIVERED",
                String.valueOf(orderId),
                saved.getCustomerEmail(),
                Map.of("orderId", orderId, "trackingNumber", saved.getTrackingNumber())
        ));
        return ApiResponse.ok(saved);
    }
}
