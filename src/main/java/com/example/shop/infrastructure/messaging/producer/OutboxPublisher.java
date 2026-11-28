package com.example.shop.infrastructure.messaging.producer;


import com.example.shop.infrastructure.messaging.repository.OutboxEventRepository;
import com.example.shop.infrastructure.outbox.entity.OutboxEvent;
import com.example.shop.infrastructure.outbox.enumeration.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final int MAX_RETRIES = 10;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 10000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findPendingEvents(
                        OutboxStatus.PENDING,
                        Instant.now()
                );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {
            kafkaTemplate.send(
                    event.getTopic(),
                    event.getMessageKey(),
                    event.getPayload()
            ).get(10, TimeUnit.SECONDS);

            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setLastError(null);

            outboxEventRepository.save(event);

            log.info(
                    "Outbox event published. EventId={}, OrderId={}",
                    event.getId(),
                    event.getAggregateId()
            );

        } catch (Exception e) {
            handleFailure(event, e);
        }
    }

    private void handleFailure(OutboxEvent event, Exception exception) {

        int retryCount = event.getRetryCount() + 1;
        event.setRetryCount(retryCount);
        event.setLastError(exception.getMessage());

        if (retryCount >= MAX_RETRIES) {
            event.setStatus(OutboxStatus.FAILED);
            event.setNextRetryAt(null);

            log.error(
                    "Outbox event permanently failed. EventId={}",
                    event.getId(),
                    exception
            );
        } else {
            event.setStatus(OutboxStatus.PENDING);
            event.setNextRetryAt(
                    Instant.now().plusSeconds(calculateBackoff(retryCount))
            );

            log.warn(
                    "Outbox event failed. EventId={}, Retry={}",
                    event.getId(),
                    retryCount,
                    exception
            );
        }

        outboxEventRepository.save(event);
    }

    private long calculateBackoff(int retryCount) {
        return Math.min(5L * (1L << (retryCount - 1)), 300L);
    }
}