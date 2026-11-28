package com.example.shop.infrastructure.messaging.repository;

import com.example.shop.infrastructure.outbox.entity.OutboxEvent;
import com.example.shop.infrastructure.outbox.enumeration.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
                SELECT e FROM OutboxEvent e
                WHERE e.status = :status
                  AND (e.nextRetryAt IS NULL OR e.nextRetryAt <= :now)
                ORDER BY e.createdAt ASC
            """)
    List<OutboxEvent> findPendingEvents(
            OutboxStatus status,
            Instant now
    );
}