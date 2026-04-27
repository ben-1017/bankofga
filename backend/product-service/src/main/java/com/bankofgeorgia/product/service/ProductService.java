package com.bankofgeorgia.product.service;

import com.bankofgeorgia.product.dto.CreateProductRequest;
import com.bankofgeorgia.product.exception.DuplicateProductException;
import com.bankofgeorgia.product.model.Product;
import com.bankofgeorgia.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(CreateProductRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateProductException("product code already exists");
        }

        Product product = new Product();
        product.setCode(request.code());
        product.setType(request.type());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setMinimumBalance(request.minimumBalance());
        product.setMonthlyFee(request.monthlyFee());
        product.setInterestRate(request.interestRate());
        product.setActive(true);
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return repository.save(product);
    }
}
