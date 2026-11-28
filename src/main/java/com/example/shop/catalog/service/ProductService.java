package com.example.shop.catalog.service;

import com.example.shop.catalog.exception.ProductNotFoundException;
import com.example.shop.catalog.mapper.ProductMapper;
import com.example.shop.catalog.model.entity.Product;
import com.example.shop.order.model.request.CreateProductRequest;
import com.example.shop.catalog.model.response.ProductResponse;
import com.example.shop.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(CreateProductRequest request) {
        Product product = productRepository.save(productMapper.toProduct(request));
        return productMapper.toProductResponse(product);
    }

    public ProductResponse getById(Long id) {
        return productMapper.toProductResponse(findProduct(id));
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
