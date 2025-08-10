package com.mkorpar.productservice.controllers;

import com.mkorpar.productservice.constants.SwaggerConstants;
import com.mkorpar.productservice.data.dtos.PageResDTO;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.rest.ErrorData;
import com.mkorpar.productservice.data.rest.ValidationErrorDataList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mkorpar.productservice.services.ProductService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import static com.mkorpar.productservice.constants.BaseConstants.PRODUCT_CONTROLLER_URL_PATH_MAPPING;
import static com.mkorpar.productservice.constants.SwaggerConstants.SECURITY_SCHEMA_NAME;

@Validated
@RestController
@RequestMapping(PRODUCT_CONTROLLER_URL_PATH_MAPPING)
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product API")
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product.",
            security = @SecurityRequirement(name = SECURITY_SCHEMA_NAME)
    )
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
                    responseCode = SwaggerConstants.UNAUTHORIZED,
                    description = "Invalid Bearer authentication token"
            ),
            @ApiResponse(
                    responseCode = SwaggerConstants.FORBIDDEN,
                    description = "Bearer authentication token not provided."
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

    @Operation(summary = "Get product list", description = "Retrieves a paginated list of products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = SwaggerConstants.OK, description = "Successfully retrieved paginated list of products."),
    })
    @Parameter(
            name = "page",
            description = "Page number (0-based)",
            example = "0",
            schema = @Schema(defaultValue = "0")
    )
    @Parameter(
            name = "size",
            description = "Number of items per page",
            example = "20",
            schema = @Schema(defaultValue = "20")
    )
    @Parameter(
            name = "sort",
            description = "Sorting criteria: property(,asc|desc)",
            example = "code,asc"
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResDTO<ProductResDTO>> getAllProducts(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

}
