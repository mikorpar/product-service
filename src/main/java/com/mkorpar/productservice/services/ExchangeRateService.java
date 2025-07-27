package com.mkorpar.productservice.services;

import com.mkorpar.productservice.clients.ExchangeRateApiClient;
import com.mkorpar.productservice.clients.enums.ExchangeRateCurrency;
import com.mkorpar.productservice.exceptions.ExchangeRateCallNotPermittedException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import com.mkorpar.productservice.exceptions.ExchangeRateUnexpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateApiClient exchangeRateApiClient;

    @Cacheable(value = "exchangeRates", key = "#currentDate")
    public Optional<BigDecimal> getEurToUsdExchangeRate(LocalDate currentDate) {
        try {
            BigDecimal exchangeRate = exchangeRateApiClient.getExchangeRateAgainstEuro(
                    ExchangeRateCurrency.USD, currentDate
            ).getMiddleRate();
            return Optional.of(exchangeRate);
        } catch(ExchangeRateUnexpectedException e) {
            log.error(e.getMessage(), e.getCause());
            return Optional.empty();
        } catch (ExchangeRateUnavailableException e) {
            log.error(e.getMessage());
            return Optional.empty();
        } catch(ExchangeRateCallNotPermittedException e) {
            log.info(e.getMessage());
            return Optional.empty();
        }
    }
}
