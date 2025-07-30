package com.mkorpar.productservice.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateService {

    Optional<BigDecimal> getEurToUsdExchangeRate(LocalDate currentDate);

}
