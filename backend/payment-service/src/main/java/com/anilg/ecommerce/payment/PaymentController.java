package com.anilg.ecommerce.payment;

import com.anilg.ecommerce.common.ApiResponse;
import com.anilg.ecommerce.common.DomainEvent;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentRepository payments;
    private final KafkaTemplate<String, DomainEvent> kafka;

    public PaymentController(PaymentRepository payments, KafkaTemplate<String, DomainEvent> kafka) {
        this.payments = payments;
        this.kafka = kafka;
    }

    @PostMapping
    public ApiResponse<Payment> pay(@RequestBody PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setCustomerEmail(request.customerEmail());
        payment.setAmount(request.amount());
        payment.setProvider(request.provider() == null ? "demo-card" : request.provider());
        payment.setStatus("PAID");
        Payment saved = payments.save(payment);
        try {
            kafka.send("commerce.events", String.valueOf(saved.getOrderId()), DomainEvent.of(
                    "PAYMENT_COMPLETED",
                    String.valueOf(saved.getOrderId()),
                    saved.getCustomerEmail(),
                    Map.of("paymentId", saved.getId(), "orderId", saved.getOrderId(), "amount", saved.getAmount())
            ));
        } catch (Exception e) {
            log.warn("Kafka send failed for payment: {}", e.getMessage());
        }
        return ApiResponse.ok(saved);
    }

    @GetMapping
    public ApiResponse<List<Payment>> byCustomer(@RequestParam String customerEmail) {
        return ApiResponse.ok(payments.findByCustomerEmailOrderByPaidAtDesc(customerEmail));
    }

    public record PaymentRequest(Long orderId, String customerEmail, BigDecimal amount, String provider) {
    }
}
