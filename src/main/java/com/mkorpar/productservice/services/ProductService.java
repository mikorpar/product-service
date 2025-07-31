package com.mkorpar.productservice.services;

import com.mkorpar.productservice.data.dtos.PageResDTO;
import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResDTO createProduct(ProductReqDTO productReqDTO);

    ProductResDTO getProduct(String code);

    PageResDTO<ProductResDTO> getAllProducts(Pageable pageable);

}
