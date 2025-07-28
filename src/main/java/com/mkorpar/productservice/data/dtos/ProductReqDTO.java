package com.mkorpar.productservice.data.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request body for creating a product.")
public class ProductReqDTO {

    @NotBlank
    @Size(min = 10, max = 10, message = "must be exactly 10 characters long")
    @Schema(description = "Unique product code, exactly 10 characters long.", example = "PRODUCT001")
    private String code;

    @NotBlank
    @Size(max = 255, message = "must be up to 255 characters long")
    @Schema(description = "Name of the product.", example = "Wireless Mouse", maxLength = 255)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(
            integer = 10,
            fraction = 2,
            message = "must be less or equal to 9999999999.99 and have up to 2 decimal places"
    )
    @Schema(description = "Price of the product in EUR.",
            example = "10.99",
            minimum = "0.01",
            maximum = "9999999999.99",
            name = "price_eur"
    )
    private BigDecimal priceEur;

    @Schema(description = "Indicates if the product is available.", defaultValue = "false")
    private boolean available;

}
