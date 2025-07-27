package com.mkorpar.productservice.mappers.mappings;

import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.exceptions.ExchangeRateUnavailableException;
import com.mkorpar.productservice.services.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPricePropertyMap extends PropertyMap<Product, ProductResDTO> {

    private final ExchangeRateService exchangeRateService;

    @Override
    protected void configure() {
        using(getEurToUsdConverter()).map(source.getPriceEur(), destination.getPriceUsd());
    }

    private Converter<BigDecimal, BigDecimal> getEurToUsdConverter() {
        return ctx -> {
            BigDecimal priceEur = ctx.getSource();
            if (priceEur == null) {
                return null;
            }

            try {
                BigDecimal exchangeRate = exchangeRateService.getEurToUsdExchangeRate(LocalDate.now());
                return priceEur.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
            } catch (ExchangeRateUnavailableException e) {
                log.error("EUR to USD exchange rate is unavailable. Exception message: {}", e.getMessage());
                return null;
            }
        };
    }

}
