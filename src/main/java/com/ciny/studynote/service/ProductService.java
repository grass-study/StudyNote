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
    public List<Product> getProducts(Long userId) {
        return productRepository.findAllByUserId(userId);
    }

    @Transactional
    public Product createProduct(ProductRequestDto requestDto, Long userId) {
        Product product = new Product(requestDto, userId);

        productRepository.save(product);

        return product;
    }

    @Transactional
    public Product updateProduct(Long id, Long userId, ProductMypriceRequestDto requestDto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NullPointerException("존재하지 않는 아이디입니다."));

        if (product.getUserId() != userId) {
            throw new IllegalArgumentException("나의 셀렉샵에 추가된 상품이 아닙니다.");
        }

        int myprice = requestDto.getMyprice();
        product.setMyprice(myprice);

        return product;
    }
}
