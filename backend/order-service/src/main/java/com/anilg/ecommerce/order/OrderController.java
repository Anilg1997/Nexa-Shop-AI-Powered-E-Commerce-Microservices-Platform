package com.anilg.ecommerce.order;

import com.anilg.ecommerce.common.ApiResponse;
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
    private final KafkaTemplate<String, String> kafka;

    public OrderController(OrderRepository orders, KafkaTemplate<String, String> kafka) {
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
        kafka.send("orders.created", String.valueOf(saved.getId()), saved.getCustomerEmail());
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
