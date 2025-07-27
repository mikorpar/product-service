package com.mkorpar.productservice.data.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResDTO {

    private String code;

    private String name;

    private BigDecimal priceEur;

    private BigDecimal priceUsd;

    private boolean available;

}
