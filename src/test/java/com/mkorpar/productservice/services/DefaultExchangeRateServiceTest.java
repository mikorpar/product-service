package com.mkorpar.productservice.services;

import com.mkorpar.productservice.clients.ExchangeRateApiClient;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.clients.data.ExchangeRateApiResponse;
import com.mkorpar.productservice.exceptions.ExchangeRateCallNotPermittedException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnexpectedException;
import com.mkorpar.productservice.services.impl.DefaultExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultExchangeRateServiceTest {

    private static final LocalDate DATE = LocalDate.of(2025, 1, 1);

    @Mock
    private ExchangeRateApiClient exchangeRateApiClient;

    @InjectMocks
    private DefaultExchangeRateService exchangeRateService;


    @Test
    void shouldReturnExchangeRate_WhenApiCallSucceeds() {
        // Arrange
        BigDecimal expectedRate = new BigDecimal("1.10");
        ExchangeRateApiResponse response = new ExchangeRateApiResponse();
        response.setMiddleRate(expectedRate);

        when(exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE))
                .thenReturn(response);

        // Act
        Optional<BigDecimal> result = exchangeRateService.getEurToUsdExchangeRate(DATE);

        // Assert
        assertThat(result).isPresent().contains(expectedRate);
    }

    @Test
    void shouldReturnEmpty_WhenExchangeRateUnexpectedExceptionThrown() {
        // Arrange
        when(exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE))
                .thenThrow(new ExchangeRateUnexpectedException("Unexpected exception", new RuntimeException()));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getEurToUsdExchangeRate(DATE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_WhenExchangeRateUnavailableExceptionThrown() {
        // Arrange
        when(exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE))
                .thenThrow(new ExchangeRateUnavailableException("Rate unavailable"));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getEurToUsdExchangeRate(DATE);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_WhenExchangeRateCallNotPermittedExceptionThrown() {
        // Arrange
        when(exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, DATE))
                .thenThrow(new ExchangeRateCallNotPermittedException("Call not permitted"));

        // Act
        Optional<BigDecimal> result = exchangeRateService.getEurToUsdExchangeRate(DATE);

        // Assert
        assertThat(result).isEmpty();
    }

}
