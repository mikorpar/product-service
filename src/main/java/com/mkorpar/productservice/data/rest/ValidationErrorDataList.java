package com.mkorpar.productservice.data.rest;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response body containing a list of validation errors.")
public record ValidationErrorDataList(
        @Schema(description = "List containing validation errors.")
        List<ValidationErrorData> errors
) {}
