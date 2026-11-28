package com.example.shop.catalog.repository;

import com.example.shop.catalog.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
