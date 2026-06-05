package com.anilg.ecommerce.shipping;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByCustomerEmailOrderByUpdatedAtDesc(String customerEmail);
    Optional<Shipment> findByOrderId(Long orderId);
}
