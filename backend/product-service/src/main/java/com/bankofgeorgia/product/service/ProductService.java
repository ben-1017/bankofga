package com.bankofgeorgia.product.service;

import com.bankofgeorgia.product.dto.CreateProductRequest;
import com.bankofgeorgia.product.dto.UpdateProductRequest;
import com.bankofgeorgia.product.exception.DuplicateProductException;
import com.bankofgeorgia.product.exception.ProductNotFoundException;
import com.bankofgeorgia.product.model.Product;
import com.bankofgeorgia.product.model.ProductType;
import com.bankofgeorgia.product.repository.ProductRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(CreateProductRequest request) {
        String code = normalizeCode(request.code());
        String name = normalizeText(request.name());
        ProductType type = request.type();

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateProductException("product code already exists");
        }
        ensureProductNameAvailable(name, type, null);

        Product product = new Product();
        product.setCode(code);
        product.setType(type);
        product.setName(name);
        product.setDescription(normalizeNullableText(request.description()));
        product.setMinimumBalance(request.minimumBalance());
        product.setMonthlyFee(request.monthlyFee());
        product.setInterestRate(request.interestRate());
        product.setActive(true);
        Instant now = Instant.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return save(product);
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));
    }

    public Product update(String id, UpdateProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found: " + id));

        String name = normalizeText(request.name());
        ProductType type = request.type();
        ensureProductNameAvailable(name, type, id);

        product.setType(type);
        product.setName(name);
        product.setDescription(normalizeNullableText(request.description()));
        product.setMinimumBalance(request.minimumBalance());
        product.setMonthlyFee(request.monthlyFee());
        product.setInterestRate(request.interestRate());
        product.setUpdatedAt(Instant.now());
        return save(product);
    }

    private Product save(Product product) {
        try {
            return repository.save(product);
        } catch (DuplicateKeyException ex) {
            // DB unique-index backstop fired (race, or a write that bypassed the
            // app-level check) — surface as a clean 409 rather than a 500.
            throw new DuplicateProductException("product code or name already exists for this type");
        }
    }

    public Product setActive(String id, boolean active) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("product not found: " + id));

        product.setActive(active);
        product.setUpdatedAt(Instant.now());
        return repository.save(product);
    }

    private void ensureProductNameAvailable(String name, ProductType type, String currentProductId) {
        repository.findFirstByNameIgnoreCaseAndType(name, type)
                .filter(existing -> !Objects.equals(existing.getId(), currentProductId))
                .ifPresent(existing -> {
                    throw new DuplicateProductException("product name already exists for this type");
                });
    }

    private String normalizeCode(String value) {
        return normalizeText(value).toUpperCase();
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullableText(String value) {
        String normalized = normalizeText(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }
}
