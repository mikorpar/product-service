package com.mkorpar.productservice.repository;

import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    private static final String PRODUCT_NAME = "Test product";
    private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(19.99);
    private static final boolean PRODUCT_AVAILABLE = true;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSuccessfullySaveProduct() {
        // Arrange
        String code = "PRODUCT001";

        // Act
        Product saved = productRepository.saveAndFlush(createProduct(code));
        Optional<Product> found = productRepository.findProductByCode(code);

        // Assert
        assertThat(found).isPresent()
                .get()
                .extracting(Product::getId, Product::getCode, Product::getName, Product::getPriceEur, Product::isAvailable)
                .containsExactly(saved.getId(), code, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_AVAILABLE);
    }

    @Test
    void shouldReturnEmptyOptional_WhenProductWithCodeDoesNotExists() {
        // Act
        Optional<Product> found = productRepository.findProductByCode("UNKNOWN");

        // Assert
        assertThat(found).isNotPresent();
    }

    @Test
    void shouldThrowException_WhenSavingProductWithDuplicateCode() {
        // Arrange
        String code = "DUPLICATE0";
        productRepository.saveAndFlush(createProduct(code));

        // Act && Assert
        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(() ->
                productRepository.saveAndFlush(createProduct(code))
        );
    }

    @Test
    void shouldNotFindProductAfterDeletion() {
        // Arrange
        String code = "TODELETE01";
        Product toDelete = productRepository.saveAndFlush(createProduct(code));

        // Act
        productRepository.delete(toDelete);
        Optional<Product> found = productRepository.findProductByCode(code);

        // Assert
        assertThat(found).isNotPresent();
    }

    private Product createProduct(String code) {
        return Product.builder()
                .code(code)
                .name(PRODUCT_NAME)
                .priceEur(PRODUCT_PRICE)
                .available(PRODUCT_AVAILABLE)
                .build();
    }

}
