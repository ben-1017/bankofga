package com.bankofgeorgia.product.repository;

import com.bankofgeorgia.product.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsByCode(String code);
}
