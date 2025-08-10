package com.mkorpar.productservice.services.impl;

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

import static com.mkorpar.productservice.constants.BaseConstants.EXCHANGE_RATE_CACHE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultExchangeRateService implements com.mkorpar.productservice.services.ExchangeRateService {

    private final ExchangeRateApiClient exchangeRateApiClient;

    @Override
    @Cacheable(value = EXCHANGE_RATE_CACHE_NAME, key = "#currentDate")
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
