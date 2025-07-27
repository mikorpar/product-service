package com.mkorpar.productservice.data.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mkorpar.productservice.config.jackson.ExchangeRateDeserializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateApiResponse {

    @JsonProperty("srednji_tecaj")
    @JsonDeserialize(using = ExchangeRateDeserializer.class)
    private BigDecimal middleRate;

}
