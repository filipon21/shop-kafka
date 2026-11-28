package com.example.shop.catalog.mapper;

import com.example.shop.catalog.model.entity.Product;
import com.example.shop.order.model.request.CreateProductRequest;
import com.example.shop.catalog.model.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toProductResponse(Product product);

    @Mapping(target = "id", ignore = true)
    Product toProduct(CreateProductRequest request);
}
