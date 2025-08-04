package com.mkorpar.productservice.clients.impl;

import com.mkorpar.productservice.clients.ExchangeRateApiClient;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.clients.data.ExchangeRateApiResponse;
import com.mkorpar.productservice.exceptions.ExchangeRateCallNotPermittedException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnexpectedException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultExchangeRateApiClient implements ExchangeRateApiClient {

    private final RestClient restClient;

    @Value("${exchange.rate.api.url.template}")
    private String urlTemplate;

    @Override
    @CircuitBreaker(name = "exchangeRateApiClient", fallbackMethod = "getExchangeRateAgainstEuroFallback")
    public ExchangeRateApiResponse getExchangeRateAgainstEuro(ExchangeRateCurrency currency, LocalDate date) {
        ResponseEntity<List<ExchangeRateApiResponse>> response = sendRequest(urlTemplate, currency, date);
        validateStatusCode(response.getStatusCode(), currency, date);

        List<ExchangeRateApiResponse> body = response.getBody();
        validateResponseBody(currency, date, body);

        log.debug("Exchange rate response body for currency={} and date={}: {}.", currency, date, body);

        return body.getFirst();
    }

    private ResponseEntity<List<ExchangeRateApiResponse>> sendRequest(String urlTemplate,
                                                                      ExchangeRateCurrency currency,
                                                                      LocalDate date) {
        return restClient.get()
                .uri(urlTemplate, currency, date)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }

    private void validateStatusCode(HttpStatusCode statusCode, ExchangeRateCurrency currency, LocalDate date) {
        if (!statusCode.equals(HttpStatus.OK)) {
            throw new ExchangeRateUnavailableException(
                    String.format("Failed to fetch exchange rate for currency %s on date %s. Status code: %d.",
                            currency, date, statusCode.value()
                    )
            );
        }
    }

    private void validateResponseBody(ExchangeRateCurrency currency, LocalDate date, List<ExchangeRateApiResponse> body) {
        if (body == null || body.isEmpty()) {
            throw new ExchangeRateUnavailableException(
                    String.format("Exchange rate not sent for currency %s on date %s.",  currency, date)
            );
        }
        if (body.size() > 1) {
            throw new ExchangeRateUnavailableException(
                    String.format("Multiple exchange rates sent for currency %s on date %s.",  currency, date)
            );
        }
    }

    private ExchangeRateApiResponse getExchangeRateAgainstEuroFallback(ExchangeRateUnavailableException e) {
        throw e;
    }

    private ExchangeRateApiResponse getExchangeRateAgainstEuroFallback(ExchangeRateCurrency currency,
                                                                       LocalDate date,
                                                                       CallNotPermittedException ignored) {
        throw new ExchangeRateCallNotPermittedException(
                String.format(
                        "Circuit breaker is open. Exchange rate is not fetched for currency %s on date %s.",
                        currency,
                        date
                )
        );
    }

    private ExchangeRateApiResponse getExchangeRateAgainstEuroFallback(ExchangeRateCurrency currency,
                                                                       LocalDate date, Throwable throwable) {
        throw new ExchangeRateUnexpectedException(
                String.format(
                        "Unexpected error happened. Exchange rate is not fetched for currency %s on date %s.",
                        currency,
                        date
                ), throwable
        );
    }

}
