package com.anilg.ecommerce.order;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orders;
    private final KafkaTemplate<String, DomainEvent> kafka;

    public OrderController(OrderRepository orders, KafkaTemplate<String, DomainEvent> kafka) {
        this.orders = orders;
        this.kafka = kafka;
    }

    @PostMapping
    public ApiResponse<PurchaseOrder> create(@RequestBody PurchaseOrder order) {
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
        PurchaseOrder saved = orders.save(order);
        kafka.send("commerce.events", String.valueOf(saved.getId()), DomainEvent.of(
                "ORDER_CREATED",
                String.valueOf(saved.getId()),
                saved.getCustomerEmail(),
                java.util.Map.of("orderId", saved.getId(), "customerEmail", saved.getCustomerEmail(), "total", saved.getTotal())
        ));
        return ApiResponse.ok(saved);
    }

    @GetMapping
    public ApiResponse<List<PurchaseOrder>> byCustomer(@RequestParam String customerEmail) {
        return ApiResponse.ok(orders.findByCustomerEmailOrderByCreatedAtDesc(customerEmail));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrder> byId(@PathVariable Long id) {
        return ApiResponse.ok(orders.findById(id).orElseThrow());
    }
}
