package com.mkorpar.productservice.clients;

import com.mkorpar.productservice.clients.impl.DefaultExchangeRateApiClient;
import com.mkorpar.productservice.config.ProductServiceConfiguration;
import com.mkorpar.productservice.data.api.ExchangeRateApiResponse;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import com.mkorpar.productservice.utils.MockServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ActiveProfiles("test")
@RestClientTest(value = {DefaultExchangeRateApiClient.class, ProductServiceConfiguration.class})
class DefaultExchangeRateApiClientTest extends MockServerTest {

    @Autowired
    private DefaultExchangeRateApiClient apiClient;

    @Test
    void shouldReturnResponseBodyWithExchangeRateOnSuccessfulRequest() {
        // Arrange
        BigDecimal expectedRate = BigDecimal.valueOf(1.0123);
        setupRestServiceServer(withSuccess(getResponseBody(expectedRate), MediaType.APPLICATION_JSON));

        // Act
        ExchangeRateApiResponse response = apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE);

        // Assert
        assertThat(response.getMiddleRate()).isEqualTo(expectedRate);
    }

    @Test
    void shouldThrowsExceptionOn200ResponseAndEmptyExchangeRateList() {
        // Arrange
        setupRestServiceServer(withSuccess(getResponseBody(), MediaType.APPLICATION_JSON));

        // Act && Assert
        assertThatExceptionOfType(ExchangeRateUnavailableException.class).isThrownBy(() ->
            apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        ).withMessageStartingWith("Exchange rate not sent");
    }

    @Test
    void shouldThrowsExceptionOn200ResponseAndMultipleExchangeRates() {
        // Arrange
        BigDecimal firstRate = BigDecimal.valueOf(1.01234);
        BigDecimal secondRate = BigDecimal.valueOf(1.02456);
        setupRestServiceServer(withSuccess(getResponseBody(firstRate, secondRate), MediaType.APPLICATION_JSON));

        // Act && Assert
        assertThatExceptionOfType(ExchangeRateUnavailableException.class).isThrownBy(() ->
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        ).withMessageStartingWith("Multiple exchange rates sent");
    }

    @Test
    void shouldThrowsExceptionOnNon200ResponseStatusCode() {
        // Arrange
        setupRestServiceServer(withServerError());

        // Act && Assert
        assertThatExceptionOfType(ExchangeRateUnavailableException.class).isThrownBy(() ->
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        ).withMessageStartingWith("Failed to fetch exchange rate for currency");
    }

}