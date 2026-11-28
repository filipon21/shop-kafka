package com.example.shop.catalog.model.response;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price) {
}
