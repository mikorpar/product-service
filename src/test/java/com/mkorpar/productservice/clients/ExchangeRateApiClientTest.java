package com.mkorpar.productservice.clients;

import com.mkorpar.productservice.config.ProductServiceConfiguration;
import com.mkorpar.productservice.data.api.ExchangeRateApiResponse;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.exceptions.ExchangeRateCallNotPermittedException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(value = {ExchangeRateApiClient.class, ProductServiceConfiguration.class}, properties = {
        "exchange.rate.api.url.template=https://api.test.com/exchange-rate/{currency}/{date}"
})
class ExchangeRateApiClientTest {

    private static final ExchangeRateCurrency CURRENCY = ExchangeRateCurrency.USD;
    private static final LocalDate DATE = LocalDate.of(2025, 1, 1);

    @Autowired
    private ExchangeRateApiClient apiClient;
    @Autowired
    private MockRestServiceServer server;

    @Value("${exchange.rate.api.url.template}")
    private String urlTemplate;

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

    @Test
    void shouldTransitionCircuitBreakerToOpenStateAndThrowException() {
        // Arrange
        setupRestServiceServer(withServerError());

        for (int requestNum = 0; requestNum < 6; requestNum++) {
            try {
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE);
            } catch (RuntimeException e) {
                // Expected exception, continue to next request
            }
        }

        // Act && Assert
        assertThatExceptionOfType(ExchangeRateCallNotPermittedException.class).isThrownBy(() ->
                apiClient.getExchangeRateAgainstEuro(CURRENCY, DATE)
        );
    }

    private String getResponseBody(BigDecimal... exchangeRates) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.GERMANY);
        numberFormat.setMinimumFractionDigits(5);
        numberFormat.setMaximumFractionDigits(5);

        String exchangesRates = Arrays.stream(exchangeRates).map(
                exchangeRate -> String.format("""
                        {"srednji_tecaj":"%s"}
                        """, numberFormat.format(exchangeRate)
                ).trim()
        ).collect(Collectors.joining(","));

        return String.format("[%s]", exchangesRates);
    }

    private void setupRestServiceServer(DefaultResponseCreator responseCreator) {
        server.expect(ExpectedCount.manyTimes(), requestTo(getRequestURI(urlTemplate, CURRENCY, DATE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(responseCreator);
    }

    private URI getRequestURI(String urlTemplate, Object... expandValues) {
        return UriComponentsBuilder
                .fromUriString(urlTemplate)
                .buildAndExpand(expandValues)
                .encode()
                .toUri();
    }

}