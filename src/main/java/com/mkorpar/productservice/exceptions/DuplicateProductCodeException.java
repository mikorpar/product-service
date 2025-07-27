package com.mkorpar.productservice.exceptions;

public class DuplicateProductCodeException extends RuntimeException {

    public DuplicateProductCodeException(String message) {
        super(message);
    }

}
