package com.mkorpar.productservice.mappers.mappings;

import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.services.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPricePropertyMapTest {

    @Mock
    private ExchangeRateService exchangeRateService;
    @InjectMocks
    private ProductPricePropertyMap productPricePropertyMap;
    
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        modelMapper.addMappings(productPricePropertyMap);
    }

    @Test
    void shouldMapPriceEurToPriceUsd_WhenExchangeRateExists() {
        // Arrange
        BigDecimal eurPrice = new BigDecimal("100.00");
        BigDecimal rate = new BigDecimal("1.1");
        BigDecimal expected = eurPrice.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        Product product = Product.builder().priceEur(eurPrice).build();

        when(exchangeRateService.getEurToUsdExchangeRate(LocalDate.now())).thenReturn(Optional.of(rate));

        // Act
        ProductResDTO result = modelMapper.map(product, ProductResDTO.class);

        // Asser
        assertThat(result.getPriceUsd()).isEqualTo(expected);
    }

    @Test
    void shouldSetPriceUsdToNull_WhenExchangeRateIsMissing() {
        // Arrange
        BigDecimal eurPrice = new BigDecimal("100.00");
        Product product = Product.builder().priceEur(eurPrice).build();

        when(exchangeRateService.getEurToUsdExchangeRate(LocalDate.now())).thenReturn(Optional.empty());

        // Act
        ProductResDTO result = modelMapper.map(product, ProductResDTO.class);

        // Assert
        assertThat(result.getPriceUsd()).isNull();
    }

    @Test
    void shouldSetPriceUsdToNull_WhenPriceEurIsNull() {
        // Arrange
        Product product = Product.builder().priceEur(null).build();

        // Act
        ProductResDTO result = modelMapper.map(product, ProductResDTO.class);

        // Assert
        assertThat(result.getPriceUsd()).isNull();
        verifyNoInteractions(exchangeRateService);
    }

}
