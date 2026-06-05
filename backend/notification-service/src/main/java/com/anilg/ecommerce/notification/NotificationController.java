package com.anilg.ecommerce.notification;

import com.anilg.ecommerce.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notifications;

    public NotificationController(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public ApiResponse<List<NotificationMessage>> byCustomer(@RequestParam String customerEmail) {
        return ApiResponse.ok(notifications.findByCustomerEmailOrderByCreatedAtDesc(customerEmail));
    }
}
