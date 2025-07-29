package com.mkorpar.productservice.data.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body containing product data.")
public class ProductResDTO {

    @Schema(description = "Unique product code, exactly 10 characters long.", example = "PRODUCT001")
    private String code;

    @Schema(description = "Name of the product.", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Price of the product in EUR.", example = "10.99", name = "price_eur")
    private BigDecimal priceEur;

    @Schema(description = "Price of the product in USD.", example = "12.89", name = "price_usd", nullable = true)
    private BigDecimal priceUsd;

    @Schema(description = "Indicates if the product is available.", example = "true")
    private boolean available;

}
