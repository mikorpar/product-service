package com.mkorpar.productservice.services;

import com.mkorpar.productservice.clients.ExchangeRateApiClient;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.clients.data.ExchangeRateApiResponse;
import com.mkorpar.productservice.data.dtos.PageResDTO;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.exceptions.DuplicateProductCodeException;
import com.mkorpar.productservice.exceptions.ProductNotFoundException;
import com.mkorpar.productservice.repositories.ProductRepository;
import com.mkorpar.productservice.services.impl.DefaultProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class DefaultProductServiceIntegrationTest {

    private final BigDecimal EXCHANGE_RATE = BigDecimal.valueOf(1.1);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DefaultProductService productService;

    @MockitoBean
    private ExchangeRateApiClient exchangeRateApiClient;

    private ProductReqDTO productToCreate;

    @BeforeEach
    void setUp() {
        productToCreate = createProductReqDTO("PRODUCT001", "Test product", 99.99, true);

        ExchangeRateApiResponse response = new ExchangeRateApiResponse();
        response.setMiddleRate(EXCHANGE_RATE);
        when(exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, LocalDate.now()))
                .thenReturn(response);
    }

    @Test
    void shouldSuccessfullyCreateProduct() {
        // Act
        ProductResDTO createdProduct = productService.createProduct(productToCreate);

        // Assert
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getCode()).isEqualTo(productToCreate.getCode());
        assertThat(createdProduct.getName()).isEqualTo(productToCreate.getName());
        assertThat(createdProduct.getPriceEur()).isEqualTo(productToCreate.getPriceEur());
        assertThat(createdProduct.getPriceUsd()).isEqualTo(
                productToCreate.getPriceEur().multiply(EXCHANGE_RATE).setScale(2, RoundingMode.HALF_UP)
        );
        assertThat(createdProduct.isAvailable()).isEqualTo(productToCreate.isAvailable());

        List<Product> allProducts = productRepository.findAll();
        assertThat(allProducts).hasSize(1);
    }

    @Test
    void shouldThrowException_WhenProductWithCodeAlreadyExists() {
        // Arrange
        productService.createProduct(productToCreate);
        ProductReqDTO duplicateProductToCreate = createProductReqDTO(
                productToCreate.getCode(), "Duplicate product", 99.99, true
        );

        // Act && Assert
        assertThatThrownBy(() ->
                productService.createProduct(duplicateProductToCreate)
        ).isInstanceOf(DuplicateProductCodeException.class);

        List<Product> allProducts = productRepository.findAll();
        assertThat(allProducts).hasSize(1);
    }

    @Test
    void shouldCreateMultipleProducts_WhenProductCodesAreDifferent() {
        // Arrange
        ProductReqDTO secondProductToCreate = createProductReqDTO(
                "PRODUCT002", "Second test product", 149.99, true
        );

        // Act
        ProductResDTO firstCreatedProduct = productService.createProduct(productToCreate);
        ProductResDTO secondCreatedProduct = productService.createProduct(secondProductToCreate);

        // Assert
        assertThat(firstCreatedProduct.getCode()).isEqualTo(productToCreate.getCode());
        assertThat(secondCreatedProduct.getCode()).isEqualTo(secondProductToCreate.getCode());

        List<Product> allProducts = productRepository.findAll();
        assertThat(allProducts).hasSize(2);
    }

    @Test
    void shouldGetProduct_WhenProductExists() {
        // Arrange
        ProductResDTO createdProduct = productService.createProduct(productToCreate);

        // Act
        ProductResDTO fetchedProduct = productService.getProduct(createdProduct.getCode());

        // Assert
        assertThat(fetchedProduct).isNotNull();
        assertThat(fetchedProduct.getCode()).isEqualTo(productToCreate.getCode());
        assertThat(fetchedProduct.getName()).isEqualTo(productToCreate.getName());
        assertThat(fetchedProduct.getPriceEur()).isEqualTo(productToCreate.getPriceEur());
        assertThat(fetchedProduct.isAvailable()).isEqualTo(productToCreate.isAvailable());
    }

    @Test
    void shouldThrowProductNotFoundException_WhenProductDoesNotExist() {
        // Act && Assert
        assertThatThrownBy(() ->
                productService.getProduct("NONEXISTENT")
        ).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldReturnEmptyList_WhenNoProductsExist() {
        // Act
        PageResDTO<ProductResDTO> result = productService.getAllProducts(Pageable.unpaged());

        // Assert
        assertThat(result.content()).isEmpty();
    }

    @Test
    void shouldGetAllProducts() {
        List<String> codesOfProductsToCreate = List.of(productToCreate.getCode(), "PRODUCT002", "PRODUCT003");
        int expectedProductCount = codesOfProductsToCreate.size();
        shouldGetProducts(codesOfProductsToCreate, Pageable.unpaged(), expectedProductCount);
    }

    @Test
    void shouldGetFirstTwoProducts() {
        int expectedProductCount = 2;
        List<String> codesOfProductsToCreate = List.of(productToCreate.getCode(), "PRODUCT002", "PRODUCT003");
        shouldGetProducts(codesOfProductsToCreate, Pageable.ofSize(expectedProductCount), expectedProductCount);
    }

    void shouldGetProducts(List<String> codesOfProductsToCreate, Pageable pageable, int expectedSize) {
        // Arrange
        codesOfProductsToCreate.stream().map(
                code -> createProductReqDTO(code, code, 100.0, true)
        ).forEach(productService::createProduct);

        // Act
        PageResDTO<ProductResDTO> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result.totalElements()).isEqualTo(codesOfProductsToCreate.size());
        assertThat(result.numberOfElements()).isEqualTo(expectedSize);
        assertThat(result.content())
                .extracting(ProductResDTO::getCode)
                .containsExactlyInAnyOrder(codesOfProductsToCreate.stream()
                        .limit(expectedSize)
                        .toArray(String[]::new)
                );
    }

    private ProductReqDTO createProductReqDTO(String code, String name, double price, boolean available) {
        return new ProductReqDTO(
                code,
                name,
                BigDecimal.valueOf(price),
                available
        );
    }

}
