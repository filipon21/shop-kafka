package com.example.shop.order.service;

import com.example.shop.infrastructure.messaging.repository.OutboxEventRepository;
import com.example.shop.infrastructure.outbox.entity.OutboxEvent;
import com.example.shop.infrastructure.outbox.enumeration.OutboxStatus;
import com.example.shop.catalog.exception.ProductNotFoundException;
import com.example.shop.catalog.model.entity.Product;
import com.example.shop.catalog.repository.ProductRepository;
import com.example.shop.order.exception.OrderNotFoundException;
import com.example.shop.order.mapper.OrderMapper;
import com.example.shop.order.model.dto.OrderCreatedEvent;
import com.example.shop.order.model.entity.Order;
import com.example.shop.order.model.entity.OrderItem;
import com.example.shop.order.model.enumeration.OrderStatus;
import com.example.shop.order.model.request.CreateOrderRequest;
import com.example.shop.order.model.request.OrderItemRequest;
import com.example.shop.order.model.response.OrderResponse;
import com.example.shop.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final String ordersTopic;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderMapper orderMapper;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.orders}") String ordersTopic,
            OutboxEventRepository outboxEventRepository,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.ordersTopic = ordersTopic;
        this.outboxEventRepository = outboxEventRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        List<OrderItemRequest> items = List.copyOf(request.items());

        Set<Long> productIds = items.stream()
                .map(OrderItemRequest::productId)
                .collect(Collectors.toSet());

        Map<Long, Product> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order newOrder = Order.builder()
                .customerId(request.customerId())
                .status(OrderStatus.PENDING)
                .build();

        items.forEach(item -> {
            Product product = products.get(item.productId());
            if (product == null) {
                throw new ProductNotFoundException(item.productId());
            }
            newOrder.getItems().add(OrderItem.builder()
                    .product(product)
                    .quantity(item.quantity())
                    .price(product.getPrice())
                    .order(newOrder)
                    .build());
        });

        BigDecimal total = newOrder.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        newOrder.setTotal(total);

        Order order = orderRepository.save(newOrder);
        createOutboxEvent(order);

        log.info("Order created: {}", order.getId());

        return orderMapper.mapOrderToCustomerOrderResponse(order);
    }

    //TODO
    @Transactional
    public OrderResponse updateOrder(CreateOrderRequest request) {
        return null;
    }

    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::mapOrderToCustomerOrderResponse)
                .toList();
    }

    private void createOutboxEvent(Order order) {

        OrderCreatedEvent event =
                orderMapper.mapOrderToOrderCreatedEvent(order);

        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateId(order.getId())
                    .eventType("ORDER_CREATED")
                    .topic(ordersTopic)
                    .messageKey(String.valueOf(order.getId()))
                    .payload(payload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .createdAt(Instant.now())
                    .build();

            outboxEventRepository.save(outboxEvent);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not create outbox event for order: " + order.getId(),
                    e
            );
        }
    }

    public OrderResponse getById(Long id) {
        return orderMapper.mapOrderToCustomerOrderResponse(findOrder(id));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
