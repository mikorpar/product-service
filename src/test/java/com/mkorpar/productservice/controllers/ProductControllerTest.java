package com.mkorpar.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.services.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        ProductReqDTO request = new ProductReqDTO(
                "PRODUCT001", "Product A", new BigDecimal("10.00"), true
        );
        ProductResDTO response = new ProductResDTO(
                "PRODUCT001", "Product A", new BigDecimal("10.00"), new BigDecimal("11.00"), true
        );

        Mockito.when(productService.createProduct(any())).thenReturn(response);

        // Act && Assert
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/products/PRODUCT001")))
                .andExpect(jsonPath("$.code").value("PRODUCT001"));
    }

    @Test
    void shouldGetProductByCode() throws Exception {
        // Arrange
        String code = "PRODUCT001";
        ProductResDTO response = new ProductResDTO(
                code, "Product A", new BigDecimal("10.00"), new BigDecimal("11.00"), true
        );

        Mockito.when(productService.getProduct(eq(code))).thenReturn(response);

        // Act && Assert
        mockMvc.perform(get("/api/v1/products/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Arrange
        List<ProductResDTO> products = List.of(
                new ProductResDTO(
                        "PRODUCT001", "Product A", new BigDecimal("10.00"), new BigDecimal("11.00"), false
                ),
                new ProductResDTO(
                        "PRODUCT002", "Product B", new BigDecimal("20.00"), new BigDecimal("22.00"), true
                )
        );

        Mockito.when(productService.getAllProducts()).thenReturn(products);

        // Act && Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PRODUCT001"))
                .andExpect(jsonPath("$[1].code").value("PRODUCT002"));
    }

    @Test
    void shouldReturn400_whenProductCodeInPathIsInvalid() throws Exception {
        // Act && Assert
        mockMvc.perform(get("/api/v1/products/{code}", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenRequestBodyIsInvalid() throws Exception {
        // Arrange
        ProductReqDTO invalidReq = new ProductReqDTO("INVALID", "Product A", new BigDecimal("10.00"), true);

        // Act && Assert
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

}
