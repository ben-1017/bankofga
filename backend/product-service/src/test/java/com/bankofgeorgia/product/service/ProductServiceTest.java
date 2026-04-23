package com.bankofgeorgia.product.service;

import com.bankofgeorgia.product.dto.CreateProductRequest;
import com.bankofgeorgia.product.exception.DuplicateProductException;
import com.bankofgeorgia.product.model.Product;
import com.bankofgeorgia.product.model.ProductType;
import com.bankofgeorgia.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository repository;
    @InjectMocks private ProductService productService;

    @Test
    void create_persistsProduct_andDefaultsActiveTrueWithTimestamps() {
        CreateProductRequest req = new CreateProductRequest(
                "CHK-STD", ProductType.CHECKING, "Standard Checking",
                "No-frills checking", new BigDecimal("100"), new BigDecimal("5"), BigDecimal.ZERO
        );
        when(repository.existsByCode("CHK-STD")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId("new-id");
            return p;
        });

        Product saved = productService.create(req);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        Product sent = captor.getValue();
        assertThat(sent.getCode()).isEqualTo("CHK-STD");
        assertThat(sent.getType()).isEqualTo(ProductType.CHECKING);
        assertThat(sent.isActive()).isTrue();
        assertThat(sent.getCreatedAt()).isNotNull();
        assertThat(sent.getUpdatedAt()).isNotNull();
        assertThat(saved.getId()).isEqualTo("new-id");
    }

    @Test
    void create_throwsDuplicate_whenCodeExists() {
        CreateProductRequest req = new CreateProductRequest(
                "CHK-STD", ProductType.CHECKING, "Standard Checking",
                null, new BigDecimal("100"), new BigDecimal("5"), BigDecimal.ZERO
        );
        when(repository.existsByCode("CHK-STD")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessage("product code already exists");

        verify(repository, never()).save(any());
    }
}
