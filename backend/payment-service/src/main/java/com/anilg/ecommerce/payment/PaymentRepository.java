package com.anilg.ecommerce.payment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerEmailOrderByPaidAtDesc(String customerEmail);
}
