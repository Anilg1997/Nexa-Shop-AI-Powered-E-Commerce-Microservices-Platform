package com.anilg.ecommerce.catalog;

import com.anilg.ecommerce.common.ApiResponse;
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
    private final KafkaTemplate<String, String> kafka;

    public CatalogController(ProductRepository products, KafkaTemplate<String, String> kafka) {
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
        kafka.send("catalog.product.created", saved.getId(), saved.getName());
        return ApiResponse.ok(saved);
    }
}
