package com.anilg.ecommerce.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
}
