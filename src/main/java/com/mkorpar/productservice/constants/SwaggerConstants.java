package com.mkorpar.productservice.constants;

import org.springframework.http.MediaType;

public final class SwaggerConstants {

    private SwaggerConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "401";
    public static final String NOT_FOUND = "404";
    public static final String CONFLICT = "409";

    public static final String SECURITY_SCHEMA_NAME = "bearerAuth";

    public static final String GRANT_TYPE = "grant_type";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TOKEN_TYPE = "token_type";
    public static final String EXPIRES_IN = "expires_in";

    public static final String TOKEN_ENDPOINT = "/oauth2/token";

    public static final String APPLICATION_FORM_URLENCODED_VALUE = MediaType.APPLICATION_FORM_URLENCODED_VALUE;
    public static final String APPLICATION_JSON = MediaType.APPLICATION_JSON_VALUE;

}
