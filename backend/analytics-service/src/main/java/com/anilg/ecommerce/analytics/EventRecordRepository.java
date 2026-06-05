package com.anilg.ecommerce.analytics;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRecordRepository extends JpaRepository<EventRecord, String> {
    List<EventRecord> findTop100ByOrderByOccurredAtDesc();
    long countByType(String type);
}
