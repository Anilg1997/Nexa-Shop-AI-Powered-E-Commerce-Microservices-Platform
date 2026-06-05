package com.anilg.ecommerce.analytics;

import com.anilg.ecommerce.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final EventRecordRepository events;

    public AnalyticsController(EventRecordRepository events) {
        this.events = events;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(Map.of(
                "registeredUsers", events.countByType("USER_REGISTERED"),
                "cartAdds", events.countByType("CART_ITEM_ADDED"),
                "orders", events.countByType("ORDER_CREATED"),
                "payments", events.countByType("PAYMENT_COMPLETED"),
                "shipments", events.countByType("ORDER_SHIPPED"),
                "deliveries", events.countByType("ORDER_DELIVERED"),
                "recentEvents", events.findTop100ByOrderByOccurredAtDesc()
        ));
    }
}
