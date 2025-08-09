package com.mkorpar.productservice.data.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.mkorpar.productservice.data.models.Product.PRODUCT_DB_TABLE_NAME;

@Data
@Entity
@Table(name = PRODUCT_DB_TABLE_NAME)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    public static final String PRODUCT_DB_TABLE_NAME = "products";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal priceEur;

    private boolean available;

}
