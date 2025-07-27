package com.mkorpar.productservice.exceptions;

public class ExchangeRateUnexpectedException extends RuntimeException {

    public ExchangeRateUnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

}
