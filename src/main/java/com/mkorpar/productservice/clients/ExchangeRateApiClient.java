package com.mkorpar.productservice.clients;

import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.clients.data.ExchangeRateApiResponse;

import java.time.LocalDate;

public interface ExchangeRateApiClient {

    ExchangeRateApiResponse getExchangeRateAgainstEuro(ExchangeRateCurrency currency, LocalDate date);

}
