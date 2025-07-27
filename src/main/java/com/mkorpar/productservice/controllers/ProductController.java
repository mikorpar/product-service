package com.mkorpar.productservice.controllers;

import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.mkorpar.productservice.services.ProductService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResDTO> createProduct(@RequestBody @Valid ProductReqDTO productReqDTO) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(productReqDTO.getCode())
                .toUri();

        return ResponseEntity.created(location).body(productService.createProduct(productReqDTO));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ProductResDTO> getProduct(@PathVariable
                                                    @NotBlank
                                                    @Size(min = 10, max = 10, message = "must be exactly 10 characters long")
                                                    String code) {
        return ResponseEntity.ok(productService.getProduct(code));
    }

    @GetMapping
    public ResponseEntity<List<ProductResDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

}
