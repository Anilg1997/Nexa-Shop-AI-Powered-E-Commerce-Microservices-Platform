package com.anilg.ecommerce.catalog;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
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
@RequestMapping("/api/catalog")
public class CatalogController {
    private final ProductRepository products;
    private final KafkaTemplate<String, Object> kafka;

    public CatalogController(ProductRepository products, KafkaTemplate<String, Object> kafka) {
        this.products = products;
        this.kafka = kafka;
    }

    @GetMapping("/products")
    public ApiResponse<List<Product>> all(@RequestParam(required = false) String q) {
        return ApiResponse.ok(q == null || q.isBlank() ? products.findAll() : products.findByNameContainingIgnoreCase(q));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Product> byId(@PathVariable String id) {
        return ApiResponse.ok(products.findById(id).orElseThrow());
    }

    @PostMapping("/products")
    public ApiResponse<Product> create(@RequestBody Product product) {
        Product saved = products.save(product);
        kafka.send("commerce.events", saved.getId(), DomainEvent.of(
                "PRODUCT_CREATED",
                saved.getId(),
                "admin",
                java.util.Map.of("name", saved.getName(), "price", saved.getPrice(), "stock", saved.getStock())
        ));
        return ApiResponse.ok(saved);
    }
}
