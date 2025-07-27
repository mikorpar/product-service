package com.mkorpar.productservice.services;

import com.mkorpar.productservice.clients.ExchangeRateApiClient;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateApiClient exchangeRateApiClient;

    public BigDecimal getEurToUsdExchangeRate(LocalDate currentDate) {
        return exchangeRateApiClient.getExchangeRateAgainstEuro(ExchangeRateCurrency.USD, currentDate).getMiddleRate();
    }

}
