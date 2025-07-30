package com.mkorpar.productservice.services;

import com.mkorpar.productservice.data.dtos.ProductReqDTO;
import com.mkorpar.productservice.data.dtos.ProductResDTO;

import java.util.List;

public interface ProductService {

    ProductResDTO createProduct(ProductReqDTO productReqDTO);

    ProductResDTO getProduct(String code);

    List<ProductResDTO> getAllProducts();

}
