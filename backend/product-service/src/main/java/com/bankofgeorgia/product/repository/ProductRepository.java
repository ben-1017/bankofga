package com.bankofgeorgia.product.repository;

import com.bankofgeorgia.product.model.Product;
import com.bankofgeorgia.product.model.ProductType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Product> findFirstByNameIgnoreCaseAndType(String name, ProductType type);
}
