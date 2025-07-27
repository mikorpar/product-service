package com.mkorpar.productservice.exceptions;

public class ExchangeRateCallNotPermittedException extends RuntimeException {

    public ExchangeRateCallNotPermittedException(String message) {
        super(message);
    }

}
