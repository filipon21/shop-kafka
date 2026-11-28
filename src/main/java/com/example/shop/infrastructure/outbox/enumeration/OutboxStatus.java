package com.example.shop.infrastructure.outbox.enumeration;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}