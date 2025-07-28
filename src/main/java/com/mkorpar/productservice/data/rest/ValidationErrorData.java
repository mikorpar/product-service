package com.mkorpar.productservice.data.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contains validation error data.")
public record ValidationErrorData(
        @Schema(description = "Name of the field for which validation failed.", example = "code")
        String field,
        @Schema(description = "Validation error message", example = "must be exactly 10 characters long")
        String message
) {}
