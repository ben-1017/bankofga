package com.bankofgeorgia.product.service;

import com.bankofgeorgia.product.dto.CreateProductRequest;
import com.bankofgeorgia.product.dto.UpdateProductRequest;
import com.bankofgeorgia.product.exception.DuplicateProductException;
import com.bankofgeorgia.product.exception.ProductNotFoundException;
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
import java.time.Instant;
import java.util.Optional;

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
                " chk-std ", ProductType.CHECKING, " Standard Checking ",
                " No-frills checking ", new BigDecimal("100"), new BigDecimal("5"), BigDecimal.ZERO
        );
        when(repository.existsByCodeIgnoreCase("CHK-STD")).thenReturn(false);
        when(repository.findFirstByNameIgnoreCaseAndType("Standard Checking", ProductType.CHECKING))
                .thenReturn(Optional.empty());
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
        assertThat(sent.getName()).isEqualTo("Standard Checking");
        assertThat(sent.getDescription()).isEqualTo("No-frills checking");
        assertThat(sent.isActive()).isTrue();
        assertThat(sent.getCreatedAt()).isNotNull();
        assertThat(sent.getUpdatedAt()).isNotNull();
        assertThat(saved.getId()).isEqualTo("new-id");
    }

    @Test
    void create_translatesDbDuplicateKeyToConflict() {
        CreateProductRequest req = new CreateProductRequest(
                "CHK-STD", ProductType.CHECKING, "Standard Checking",
                null, new BigDecimal("100"), new BigDecimal("5"), BigDecimal.ZERO
        );
        when(repository.existsByCodeIgnoreCase("CHK-STD")).thenReturn(false);
        when(repository.findFirstByNameIgnoreCaseAndType("Standard Checking", ProductType.CHECKING))
                .thenReturn(Optional.empty());
        when(repository.save(any(Product.class)))
                .thenThrow(new org.springframework.dao.DuplicateKeyException("E11000 duplicate key"));

        // DB unique-index backstop must surface as a domain 409, not a 500.
        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(DuplicateProductException.class);
    }

    @Test
    void create_throwsDuplicate_whenCodeExists() {
        CreateProductRequest req = new CreateProductRequest(
                "CHK-STD", ProductType.CHECKING, "Standard Checking",
                null, new BigDecimal("100"), new BigDecimal("5"), BigDecimal.ZERO
        );
        when(repository.existsByCodeIgnoreCase("CHK-STD")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessage("product code already exists");

        verify(repository, never()).save(any());
    }

    @Test
    void create_throwsDuplicate_whenNameAlreadyExistsForType() {
        CreateProductRequest req = new CreateProductRequest(
                "CHK-PREM", ProductType.CHECKING, "Premium Checking",
                null, new BigDecimal("100"), new BigDecimal("5"), BigDecimal.ZERO
        );
        Product existing = new Product();
        existing.setId("p-existing");

        when(repository.existsByCodeIgnoreCase("CHK-PREM")).thenReturn(false);
        when(repository.findFirstByNameIgnoreCaseAndType("Premium Checking", ProductType.CHECKING))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessage("product name already exists for this type");

        verify(repository, never()).save(any());
    }

    @Test
    void update_overwritesMutableFields_andBumpsUpdatedAt() {
        Instant originalCreatedAt = Instant.parse("2026-04-01T00:00:00Z");
        Instant originalUpdatedAt = Instant.parse("2026-04-10T00:00:00Z");
        Product existing = new Product();
        existing.setId("p-1");
        existing.setCode("CHK-STD");
        existing.setType(ProductType.CHECKING);
        existing.setName("Old Name");
        existing.setActive(true);
        existing.setCreatedAt(originalCreatedAt);
        existing.setUpdatedAt(originalUpdatedAt);

        UpdateProductRequest req = new UpdateProductRequest(
                ProductType.SAVINGS, " New Name ", " updated description ",
                new BigDecimal("250"), new BigDecimal("0"), new BigDecimal("0.025")
        );
        when(repository.findById("p-1")).thenReturn(Optional.of(existing));
        when(repository.findFirstByNameIgnoreCaseAndType("New Name", ProductType.SAVINGS))
                .thenReturn(Optional.empty());
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update("p-1", req);

        assertThat(result.getCode()).isEqualTo("CHK-STD");
        assertThat(result.getType()).isEqualTo(ProductType.SAVINGS);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("updated description");
        assertThat(result.getMinimumBalance()).isEqualByComparingTo("250");
        assertThat(result.getInterestRate()).isEqualByComparingTo("0.025");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(result.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void update_throwsNotFound_whenIdMissing() {
        UpdateProductRequest req = new UpdateProductRequest(
                ProductType.CHECKING, "x", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update("missing", req))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("missing");

        verify(repository, never()).save(any());
    }

    @Test
    void update_allowsSameProductNameForCurrentProduct() {
        Product existing = new Product();
        existing.setId("p-1");
        existing.setType(ProductType.CHECKING);
        existing.setName("Standard Checking");
        existing.setUpdatedAt(Instant.parse("2026-04-10T00:00:00Z"));

        UpdateProductRequest req = new UpdateProductRequest(
                ProductType.CHECKING, "Standard Checking", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(repository.findById("p-1")).thenReturn(Optional.of(existing));
        when(repository.findFirstByNameIgnoreCaseAndType("Standard Checking", ProductType.CHECKING))
                .thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update("p-1", req);

        assertThat(result.getName()).isEqualTo("Standard Checking");
        verify(repository).save(existing);
    }

    @Test
    void update_throwsDuplicate_whenNameBelongsToAnotherProductOfSameType() {
        Product existing = new Product();
        existing.setId("p-1");
        existing.setType(ProductType.CHECKING);
        existing.setName("Standard Checking");

        Product other = new Product();
        other.setId("p-2");

        UpdateProductRequest req = new UpdateProductRequest(
                ProductType.CHECKING, "Premium Checking", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(repository.findById("p-1")).thenReturn(Optional.of(existing));
        when(repository.findFirstByNameIgnoreCaseAndType("Premium Checking", ProductType.CHECKING))
                .thenReturn(Optional.of(other));

        assertThatThrownBy(() -> productService.update("p-1", req))
                .isInstanceOf(DuplicateProductException.class)
                .hasMessage("product name already exists for this type");

        verify(repository, never()).save(any());
    }

    @Test
    void setActive_togglesStatus_andBumpsUpdatedAt() {
        Instant originalUpdatedAt = Instant.parse("2026-04-10T00:00:00Z");
        Product existing = new Product();
        existing.setId("p-1");
        existing.setActive(true);
        existing.setUpdatedAt(originalUpdatedAt);

        when(repository.findById("p-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.setActive("p-1", false);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    void setActive_throwsNotFound_whenIdMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.setActive("missing", false))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("missing");

        verify(repository, never()).save(any());
    }
}
