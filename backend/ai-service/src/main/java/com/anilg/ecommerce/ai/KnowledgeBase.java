package com.anilg.ecommerce.ai;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBase {
    private static final List<String> DOCUMENTS = List.of(
            "Orders can be created after products are added to cart and customer email is provided.",
            "Catalog products include name, description, price, stock, and image URL.",
            "The local AI model runs through Ollama at http://localhost:11434.",
            "Kafka uses the unified commerce.events topic for all ecommerce lifecycle events.",
            "PostgreSQL stores auth, order, payment, shipping, notification, and analytics data.",
            "Redis stores live shopping carts for fast add, delete, and clear operations.",
            "The commerce.events Kafka topic tracks USER_REGISTERED, USER_LOGGED_IN, CART_ITEM_ADDED, CART_ITEM_REMOVED, ORDER_CREATED, PAYMENT_COMPLETED, ORDER_SHIPPED, and ORDER_DELIVERED.",
            "Analytics service consumes commerce.events and exposes /api/analytics/dashboard for real-time funnel analysis.",
            "Notification service listens to lifecycle events and records demo email notifications."
    );

    public List<String> search(String query) {
        String normalized = query == null ? "" : query.toLowerCase();
        return DOCUMENTS.stream()
                .filter(doc -> normalized.isBlank() || containsAnyTerm(doc, normalized))
                .limit(3)
                .toList();
    }

    private boolean containsAnyTerm(String document, String query) {
        String lowerDocument = document.toLowerCase();
        for (String term : query.split("\\W+")) {
            if (!term.isBlank() && lowerDocument.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
