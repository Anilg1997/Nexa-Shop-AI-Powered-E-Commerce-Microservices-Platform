package com.anilg.ecommerce.catalog;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);
    private final ProductRepository products;
    private final ReviewRepository reviews;
    private final KafkaTemplate<String, Object> kafka;

    public CatalogController(ProductRepository products, ReviewRepository reviews,
                             KafkaTemplate<String, Object> kafka) {
        this.products = products;
        this.reviews = reviews;
        this.kafka = kafka;
    }

    @GetMapping("/products")
    public ApiResponse<List<Product>> all(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) String category) {
        if (q != null && !q.isBlank()) return ApiResponse.ok(products.findByNameContainingIgnoreCase(q));
        if (category != null && !category.isBlank()) return ApiResponse.ok(products.findByCategoryIgnoreCase(category));
        return ApiResponse.ok(products.findAll());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Product> byId(@PathVariable String id) {
        return ApiResponse.ok(products.findById(id).orElseThrow());
    }

    @PostMapping("/products")
    public ApiResponse<Product> create(@RequestBody Product product) {
        Product saved = products.save(product);
        try {
            kafka.send("commerce.events", saved.getId(), DomainEvent.of(
                    "PRODUCT_CREATED", saved.getId(), "admin",
                    Map.of("name", saved.getName(), "price", saved.getPrice(), "stock", saved.getStock(),
                           "category", saved.getCategory())
            ));
        } catch (Exception e) {
            log.warn("Kafka send failed for product create: {}", e.getMessage());
        }
        return ApiResponse.ok(saved);
    }

    @PutMapping("/products/{id}")
    public ApiResponse<Product> update(@PathVariable String id, @RequestBody Product product) {
        Product existing = products.findById(id).orElseThrow();
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        existing.setCategory(product.getCategory());
        existing.setRating(product.getRating());
        existing.setFeatures(product.getFeatures());
        return ApiResponse.ok(products.save(existing));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        products.deleteById(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/products/{id}/reviews")
    public ApiResponse<List<Review>> getReviews(@PathVariable String id) {
        return ApiResponse.ok(reviews.findByProductId(id));
    }

    @PostMapping("/products/{id}/reviews")
    public ApiResponse<Review> addReview(@PathVariable String id, @RequestBody Review review) {
        review.setProductId(id);
        Review saved = reviews.save(review);
        products.findById(id).ifPresent(p -> {
            List<Review> all = reviews.findByProductId(id);
            double avg = all.stream().mapToInt(Review::getRating).average().orElse(0);
            p.setRating(Math.round(avg * 10) / 10.0);
            p.setReviewCount(all.size());
            products.save(p);
        });
        return ApiResponse.ok(saved);
    }
}
