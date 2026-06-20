package com.anilg.ecommerce.cart;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final RedisTemplate<String, CartItem> redis;
    private final KafkaTemplate<String, DomainEvent> kafka;

    public CartController(RedisTemplate<String, CartItem> redis, KafkaTemplate<String, DomainEvent> kafka) {
        this.redis = redis;
        this.kafka = kafka;
    }

    @GetMapping
    public ApiResponse<List<CartItem>> get(@RequestParam String customerEmail) {
        return ApiResponse.ok(items(customerEmail));
    }

    @PostMapping("/items")
    public ApiResponse<List<CartItem>> add(@RequestParam String customerEmail, @RequestBody AddCartItem request) {
        List<CartItem> items = items(customerEmail);
        int quantity = request.quantity() <= 0 ? 1 : request.quantity();
        items.removeIf(item -> item.productId().equals(request.productId()));
        items.add(new CartItem(request.productId(), request.productName(), request.unitPrice(), quantity, request.imageUrl()));
        save(customerEmail, items);
        publish("CART_ITEM_ADDED", customerEmail, Map.of("productId", request.productId(), "quantity", quantity));
        return ApiResponse.ok(items);
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<List<CartItem>> delete(@RequestParam String customerEmail, @PathVariable String productId) {
        List<CartItem> items = items(customerEmail);
        items.removeIf(item -> item.productId().equals(productId));
        save(customerEmail, items);
        publish("CART_ITEM_REMOVED", customerEmail, Map.of("productId", productId));
        return ApiResponse.ok(items);
    }

    @DeleteMapping
    public ApiResponse<List<CartItem>> clear(@RequestParam String customerEmail) {
        redis.delete(key(customerEmail));
        publish("CART_CLEARED", customerEmail, Map.of("customerEmail", customerEmail));
        return ApiResponse.ok(List.of());
    }

    private List<CartItem> items(String customerEmail) {
        List<CartItem> items = redis.opsForList().range(key(customerEmail), 0, -1);
        return items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    private void save(String customerEmail, List<CartItem> items) {
        String key = key(customerEmail);
        redis.delete(key);
        if (!items.isEmpty()) {
            redis.opsForList().rightPushAll(key, items);
        }
    }

    private void publish(String type, String customerEmail, Map<String, Object> payload) {
        try {
            kafka.send("commerce.events", customerEmail, DomainEvent.of(type, customerEmail, customerEmail, payload));
        } catch (Exception e) {
            log.warn("Kafka send failed for cart event: {}", e.getMessage());
        }
    }

    private String key(String customerEmail) {
        return "cart:" + customerEmail.toLowerCase();
    }

    public record AddCartItem(String productId, String productName, BigDecimal unitPrice, int quantity, String imageUrl) {
    }
}
