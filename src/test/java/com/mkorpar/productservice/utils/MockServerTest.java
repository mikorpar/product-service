package com.mkorpar.productservice.utils;

import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

public abstract class MockServerTest {

    protected static final ExchangeRateCurrency CURRENCY = ExchangeRateCurrency.USD;
    protected static final LocalDate DATE = LocalDate.of(2025, 1, 1);

    @Autowired
    private MockRestServiceServer mockServer;

    @Value("${exchange.rate.api.url.template}")
    private String urlTemplate;

    protected void setupRestServiceServer(ResponseCreator responseCreator) {
        mockServer.expect(ExpectedCount.manyTimes(), requestTo(getRequestURI(urlTemplate, CURRENCY, DATE)))
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

    protected String getResponseBody(BigDecimal... exchangeRates) {
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

}
