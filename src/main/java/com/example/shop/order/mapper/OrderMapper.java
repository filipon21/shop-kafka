package com.example.shop.order.mapper;

import com.example.shop.order.model.dto.OrderCreatedEvent;
import com.example.shop.order.model.entity.Order;
import com.example.shop.order.model.entity.OrderItem;
import com.example.shop.order.model.request.OrderItemRequest;
import com.example.shop.order.model.response.OrderItemResponse;
import com.example.shop.order.model.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse mapOrderToCustomerOrderResponse(Order customerOrder);

    @Mapping(target = "orderId", source = "id")
    OrderCreatedEvent mapOrderToOrderCreatedEvent(Order order);

    @Mapping(target = "productId", source = "product.id")
    OrderItemRequest mapOrderItemToOrderItemRequest(OrderItem orderItem);

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse mapOrderItemToOrderItemResponse(OrderItem orderItem);
}
