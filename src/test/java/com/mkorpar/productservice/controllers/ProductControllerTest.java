package com.mkorpar.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mkorpar.productservice.security.SecurityConfig;
import com.mkorpar.productservice.data.dtos.PageResDTO;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.services.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.mkorpar.productservice.constants.BaseConstants.PRODUCT_CONTROLLER_URL_PATH_MAPPING;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String ENDPOINT = PRODUCT_CONTROLLER_URL_PATH_MAPPING;

    private static final ProductReqDTO productReqDTO = new ProductReqDTO(
            "PRODUCT001", "Product A", new BigDecimal("10.00"), true
    );
    private static final ProductResDTO productResDTO = new ProductResDTO(
            "PRODUCT001", "Product A", new BigDecimal("10.00"), new BigDecimal("11.00"), false
    );
    private static final ProductResDTO secondProductResDTO = new ProductResDTO(
            "PRODUCT002", "Product B", new BigDecimal("20.00"), new BigDecimal("22.00"), true
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        Mockito.when(productService.createProduct(any())).thenReturn(productResDTO);

        // Act && Assert
        mockMvc.perform(post(ENDPOINT)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productReqDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(ENDPOINT + "/" + productReqDTO.getCode())))
                .andExpect(jsonPath("$.code").value(productReqDTO.getCode()));
    }

    @Test
    void shouldReturn400_whenRequestBodyIsInvalid() throws Exception {
        // Arrange
        ProductReqDTO invalidReq = new ProductReqDTO("INVALID", "Product A", new BigDecimal("10.00"), true);

        // Act && Assert
        mockMvc.perform(post(ENDPOINT)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn403_whenAuthTokenIsNotProvided() throws Exception {
        // Act && Assert
        mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productReqDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetProductByCode() throws Exception {
        // Arrange
        String code = productReqDTO.getCode();
        Mockito.when(productService.getProduct(eq(code))).thenReturn(productResDTO);

        // Act && Assert
        mockMvc.perform(get(ENDPOINT + "/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.name").value(productReqDTO.getName()));
    }

    @Test
    void shouldReturn400_whenProductCodeInPathIsInvalid() throws Exception {
        // Act && Assert
        mockMvc.perform(get(ENDPOINT + "/{code}", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        // Arrange
        List<ProductResDTO> products = List.of(productResDTO, secondProductResDTO);
        Mockito.when(productService.getAllProducts(any())).thenReturn(PageResDTO.<ProductResDTO>builder()
                .content(products)
                .build()
        );

        // Act && Assert
        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[0].code").value(products.getFirst().getCode()))
                .andExpect(jsonPath("$.content.[1].code").value(products.getLast().getCode()));
    }

}
