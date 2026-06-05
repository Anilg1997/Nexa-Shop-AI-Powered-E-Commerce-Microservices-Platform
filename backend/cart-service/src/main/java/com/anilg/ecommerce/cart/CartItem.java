package com.anilg.ecommerce.cart;

import java.math.BigDecimal;

public record CartItem(String productId, String productName, BigDecimal unitPrice, int quantity, String imageUrl) {
}
