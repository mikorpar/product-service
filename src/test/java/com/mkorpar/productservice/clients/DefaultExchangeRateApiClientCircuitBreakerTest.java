package com.mkorpar.productservice.clients;

import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.clients.impl.DefaultExchangeRateApiClient;
import com.mkorpar.productservice.exceptions.ExchangeRateCallNotPermittedException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnexpectedException;
import com.mkorpar.productservice.utils.MockServerTest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockRestServiceServer
public class DefaultExchangeRateApiClientCircuitBreakerTest extends MockServerTest {

    private static final ExchangeRateCurrency CURRENCY = ExchangeRateCurrency.USD;
    private static final LocalDate DATE = LocalDate.of(2025, 1, 1);

    @Autowired
    private DefaultExchangeRateApiClient apiClient;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void getCircuitBreaker() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("exchangeRateApiClient");
        circuitBreaker.reset();
    }

    @Test
    void shouldReturnResponse_WhenRequestIsSuccessful() {
        // Arrange
        BigDecimal expectedRate = BigDecimal.valueOf(1.01234);
        setupRestServiceServer(withSuccess(getResponseBody(expectedRate), MediaType.APPLICATION_JSON));

        // Act
        BigDecimal actualRate = apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE).getMiddleRate();

        // Asser
        assertThat(actualRate).isEqualTo(expectedRate);
    }

    @Test
    void shouldRethrowExceptionFromFallbackMethod_WhenRequestIsNotSuccessful() {
        // Arrange
        setupRestServiceServer(withServerError());

        // Act && Assert
        assertThatExceptionOfType(ExchangeRateUnavailableException.class).isThrownBy(() ->
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        );
    }

    @Test
    void shouldThrowCallNotPermittedException_WhenCircuitBreakerIsOpenAndCallIsDone() {
        // Arrange
        setupRestServiceServer(withServerError());

        int minNumOfCallsForStateTransition = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();
        for (int requestTry = 0; requestTry < minNumOfCallsForStateTransition; requestTry++) {
            try {
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE);
            } catch (ExchangeRateUnavailableException ignored) {
                // Expected exception, continue to next iteration
            }
        }

        // Act && Assert
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThatExceptionOfType(ExchangeRateCallNotPermittedException.class).isThrownBy(() ->
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        );
    }

    @Test
    void shouldThrowExchangeRateUnexpectedException_WhenCircuitBreakerIsClosedAndUnexpectExceptionHappened() {
        // Arrange
        SocketTimeoutException expectedException = new SocketTimeoutException("Simulated timeout");
        setupRestServiceServer(withException(expectedException));

        // Act && Assert
        assertThatExceptionOfType(ExchangeRateUnexpectedException.class).isThrownBy(() ->
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        ).withCauseInstanceOf(ResourceAccessException.class)
                .withRootCauseInstanceOf(expectedException.getClass());
    }

}

