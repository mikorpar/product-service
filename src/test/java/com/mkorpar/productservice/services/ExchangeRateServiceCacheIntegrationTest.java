package com.mkorpar.productservice.services;

import com.mkorpar.productservice.clients.ExchangeRateApiClient;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.data.api.ExchangeRateApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceCacheIntegrationTest {

    private static final String CACHE_NAME = "exchangeRates";
    private static final LocalDate DATE = LocalDate.of(2025, 1, 1);

    @MockitoBean
    private ExchangeRateApiClient exchangeRateApiClient;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        Optional.ofNullable(cacheManager.getCache(CACHE_NAME))
                .ifPresent(Cache::clear);
    }

    @Test
    void shouldReturnCachedValue_WhenApiCallIsRepeatedWithTheSameDate() {
        // Arrange
        BigDecimal expectedRate = new BigDecimal("1.10");
        ExchangeRateApiResponse response = new ExchangeRateApiResponse();
        response.setMiddleRate(expectedRate);

        when(exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE))
                .thenReturn(response);

        // Act && Assert
        verifyNoInteractions(exchangeRateApiClient);
        Optional<BigDecimal> exchangeRate;

        exchangeRate = exchangeRateService.getEurToUsdExchangeRate(DATE);
        assertThat(exchangeRate).containsSame(expectedRate);
        verify(exchangeRateApiClient, times(1))
                .getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE);

        exchangeRate = exchangeRateService.getEurToUsdExchangeRate(DATE);
        assertThat(exchangeRate).containsSame(expectedRate);
        verify(exchangeRateApiClient, times(1))
                .getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE);
    }

}

