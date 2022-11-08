package com.ciny.studynote.service;

import com.ciny.studynote.dto.ProductMypriceRequestDto;
import com.ciny.studynote.dto.ProductRequestDto;
import com.ciny.studynote.model.Product;
import com.ciny.studynote.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        List<Product> products = productRepository.findAll();

        return products;
    }

    @Transactional
    public Product createProduct(ProductRequestDto requestDto) {
        Product product = new Product(requestDto);

        productRepository.save(product);

        return product;
    }

    @Transactional
    public Product updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NullPointerException("존재하지 않는 아이디입니다."));

        int myprice = requestDto.getMyprice();
        product.setMyprice(myprice);

        return product;
    }
}
