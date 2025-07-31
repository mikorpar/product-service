package com.mkorpar.productservice.controllers;

import com.mkorpar.productservice.constants.SwaggerConstants;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.rest.ErrorData;
import com.mkorpar.productservice.data.rest.ValidationErrorDataList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mkorpar.productservice.services.ProductService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.CREATED, description = "Product created successfully."),
            @ApiResponse(
                    responseCode = SwaggerConstants.BAD_REQUEST,
                    description = "Invalid request body.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorDataList.class)
                    )
            ),
            @ApiResponse(
                    responseCode = SwaggerConstants.CONFLICT,
                    description = "Product with provided code already exits.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorData.class)
                    )
            )
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResDTO> createProduct(@RequestBody @Valid ProductReqDTO productReqDTO) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(productReqDTO.getCode())
                .toUri();

        return ResponseEntity.created(location).body(productService.createProduct(productReqDTO));
    }

    @Operation(summary = "Get product by code", description = "Retrieves a product code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.OK, description = "Product retrieved successfully."),
            @ApiResponse(
                    responseCode = SwaggerConstants.BAD_REQUEST,
                    description = "Invalid product code.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorDataList.class)
                    )
            ),
            @ApiResponse(responseCode = SwaggerConstants.NOT_FOUND,
                    description = "Product not found.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorData.class)
                    )
            )
    })
    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResDTO> getProduct(@PathVariable
                                                    @NotBlank
                                                    @Size(min = 10, max = 10, message = "must be exactly 10 characters long")
                                                    String code) {
        return ResponseEntity.ok(productService.getProduct(code));
    }

    @Operation(summary = "Get all products", description = "Retrieves a list of all products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.OK, description = "Successfully retrieved list of products."),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductResDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

}
