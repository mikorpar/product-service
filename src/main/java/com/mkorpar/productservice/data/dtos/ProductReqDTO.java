package com.mkorpar.productservice.data.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductReqDTO {

    @NotBlank
    @Size(min = 10, max = 10, message = "must be exactly 10 characters long")
    private String code;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2, message = "must be less or equal to 9999999999.99 and have up to 2 decimal places")
    private BigDecimal priceEur;

    private boolean available;

}
