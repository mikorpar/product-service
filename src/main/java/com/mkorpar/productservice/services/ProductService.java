package com.mkorpar.productservice.services;

import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.exceptions.DuplicateProductCodeException;
import com.mkorpar.productservice.exceptions.ProductNotFoundException;
import com.mkorpar.productservice.mappers.CoreModelMapper;
import com.mkorpar.productservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CoreModelMapper modelMapper;
    private final ProductRepository productRepository;

    public ProductResDTO createProduct(ProductReqDTO productReqDTO) {
        String productCode = productReqDTO.getCode();
        if (productRepository.findProductByCode(productCode).isPresent()) {
            throw new DuplicateProductCodeException(String.format("Product with code %s already exists.", productCode));
        }

        Product product = modelMapper.map(productReqDTO, Product.class);
        productRepository.save(product);
        return modelMapper.map(productRepository.save(product), ProductResDTO.class);
    }

    public ProductResDTO getProduct(String code) {
        Product product = productRepository.findProductByCode(code)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with code %s not found.", code)));
        return modelMapper.map(product, ProductResDTO.class);
    }

    public List<ProductResDTO> getAllProducts() {
        return modelMapper.mapList(productRepository.findAll(), ProductResDTO.class);
    }

}
