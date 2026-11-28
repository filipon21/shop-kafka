package com.example.shop.order.model.response;

import java.math.BigDecimal;

public record OrderItemResponse(Long productId, int quantity, BigDecimal price) {
}
