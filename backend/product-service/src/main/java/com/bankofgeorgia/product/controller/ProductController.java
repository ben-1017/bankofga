package com.bankofgeorgia.product.controller;

import com.bankofgeorgia.product.dto.CreateProductRequest;
import com.bankofgeorgia.product.dto.ProductResponse;
import com.bankofgeorgia.product.dto.UpdateProductRequest;
import com.bankofgeorgia.product.dto.UpdateProductStatusRequest;
import com.bankofgeorgia.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse body = ProductResponse.from(productService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list() {
        List<ProductResponse> body = productService.findAll().stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(ProductResponse.from(productService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable String id,
                                                  @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponse> setStatus(@PathVariable String id,
                                                     @Valid @RequestBody UpdateProductStatusRequest request) {
        return ResponseEntity.ok(ProductResponse.from(productService.setActive(id, request.active())));
    }
}
