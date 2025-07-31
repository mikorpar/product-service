package com.mkorpar.productservice.services.impl;

import com.mkorpar.productservice.data.dtos.PageResDTO;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import com.mkorpar.productservice.data.models.Product;
import com.mkorpar.productservice.exceptions.DuplicateProductCodeException;
import com.mkorpar.productservice.exceptions.ProductNotFoundException;
import com.mkorpar.productservice.mappers.CoreModelMapper;
import com.mkorpar.productservice.repositories.ProductRepository;
import com.mkorpar.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultProductService implements ProductService {

    private final CoreModelMapper modelMapper;
    private final ProductRepository productRepository;

    @Override
    public ProductResDTO createProduct(ProductReqDTO productReqDTO) {
        String productCode = productReqDTO.getCode();
        if (productRepository.findProductByCode(productCode).isPresent()) {
            throw new DuplicateProductCodeException(String.format("Product with code %s already exists.", productCode));
        }

        Product product = modelMapper.map(productReqDTO, Product.class);
        productRepository.save(product);
        return modelMapper.map(productRepository.save(product), ProductResDTO.class);
    }

    @Override
    public ProductResDTO getProduct(String code) {
        Product product = productRepository.findProductByCode(code)
                .orElseThrow(() -> new ProductNotFoundException(String.format("Product with code %s not found.", code)));
        return modelMapper.map(product, ProductResDTO.class);
    }

    @Override
    public PageResDTO<ProductResDTO> getAllProducts(Pageable pageable) {
        Page<Product> foundProductsPage = productRepository.findAll(pageable);
        return PageResDTO.from(
                foundProductsPage,
                modelMapper.mapList(foundProductsPage.getContent(), ProductResDTO.class)
        );
    }

}
