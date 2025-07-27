package com.mkorpar.productservice.mappers.mappings;

import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.services.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

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

            return exchangeRateService.getEurToUsdExchangeRate(LocalDate.now())
                    .map(exchangeRate -> priceEur.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP))
                    .orElse(null);
        };
    }

}
