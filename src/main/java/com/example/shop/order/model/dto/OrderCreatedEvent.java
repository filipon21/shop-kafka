package com.example.shop.order.model.dto;

import com.example.shop.order.model.request.OrderItemRequest;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        List<OrderItemRequest> items,
        BigDecimal total
) {
}
