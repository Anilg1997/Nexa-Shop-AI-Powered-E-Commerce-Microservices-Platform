package com.anilg.ecommerce.ai;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBase {
    private static final List<String> DOCUMENTS = List.of(
            "Orders can be created after products are added to cart and customer email is provided.",
            "Catalog products include name, description, price, stock, and image URL.",
            "The local AI model runs through Ollama at http://localhost:11434.",
            "Kafka topics used by the platform include catalog.product.created and orders.created.",
            "PostgreSQL stores auth and order data; MongoDB stores catalog data."
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
