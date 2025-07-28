package com.mkorpar.productservice.data.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body containing error data.")
public record ErrorData(
        @Schema(description = "Error name.", example = "DuplicateProductCodeException")
        String error,
        @Schema(description = "Error message.", example = "Product with code PRODUCT001 already exists.")
        String message
) {}
