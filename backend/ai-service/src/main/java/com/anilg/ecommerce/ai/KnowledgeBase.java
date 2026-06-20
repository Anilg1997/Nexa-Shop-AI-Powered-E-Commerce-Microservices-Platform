package com.anilg.ecommerce.ai;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Entity
@Table(name = "knowledge_docs")
class KnowledgeDoc {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String category;
    @Lob
    private String content;
    private String embedding;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
}

@Repository
interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {
    List<KnowledgeDoc> findByCategory(String category);
    @Query(value = "SELECT * FROM knowledge_docs WHERE LOWER(content) LIKE LOWER(CONCAT('%', ?1, '%')) ORDER BY id LIMIT ?2", nativeQuery = true)
    List<KnowledgeDoc> findRelevant(String query, int limit);
}

@Component
public class KnowledgeBase {
    private final KnowledgeDocRepository repo;

    public KnowledgeBase(KnowledgeDocRepository repo) {
        this.repo = repo;
        if (repo.count() == 0) {
            seed();
        }
    }

    public List<String> search(String query) {
        String normalized = query == null ? "" : query.toLowerCase();
        return repo.findAll().stream()
                .filter(doc -> normalized.isBlank() || doc.getContent().toLowerCase().contains(normalized))
                .limit(5)
                .map(KnowledgeDoc::getContent)
                .toList();
    }

    private void seed() {
        String[][] docs = {
            {"System Architecture", "The platform uses microservices with Spring Boot, connected via Eureka and API Gateway. Events flow through Kafka topic commerce.events."},
            {"Product Catalog", "Catalog products include id, name, description, price, stock, imageUrl, category, rating, and features. Stored in MongoDB."},
            {"Shopping Cart", "Carts are stored in Redis as lists of CartItem objects. Each cart is keyed by customer email: cart:<email>."},
            {"Order Lifecycle", "Orders flow through states: CREATED -> PAID -> SHIPPED -> DELIVERED. Each transition emits a Kafka event."},
            {"AI Services", "The AI service uses Spring AI with Ollama for LLM inference. Supports RAG, agentic analysis, and MCP tool execution."},
            {"Event Tracking", "The analytics service consumes all commerce.events and provides a real-time dashboard with funnel metrics and recent events."},
            {"Authentication", "JWT-based auth with roles: CUSTOMER and ADMIN. Tokens include email and role claims. Auth service manages users."},
            {"Kafka Events", "Events: USER_REGISTERED, USER_LOGGED_IN, PRODUCT_CREATED, CART_ITEM_ADDED, CART_ITEM_REMOVED, ORDER_CREATED, PAYMENT_COMPLETED, ORDER_SHIPPED, ORDER_DELIVERED."},
            {"Notifications", "The notification service listens to lifecycle events and creates demo email notifications. Events trigger personalized messages."},
            {"Vector Search", "RAG uses pgvector for embedding-based similarity search. Knowledge base documents are stored in PostgreSQL with vector embeddings."}
        };
        for (String[] doc : docs) {
            KnowledgeDoc kd = new KnowledgeDoc();
            kd.setTitle(doc[0]);
            kd.setCategory("system");
            kd.setContent(doc[1]);
            repo.save(kd);
        }
    }
}
