package com.mkorpar.productservice.constants;

public class BaseConstants {

    private BaseConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String PRODUCT_CONTROLLER_URL_PATH_MAPPING = "/api/v1/products";
    public static final String API_CLIENT_CIRCUIT_BREAKER_NAME = "exchangeRateApiClient";
    public static final String EXCHANGE_RATE_CACHE_NAME = "exchangeRates";

}
