package com.ciny.studynote.service;

import com.ciny.studynote.dto.ProductMypriceRequestDto;
import com.ciny.studynote.dto.ProductRequestDto;
import com.ciny.studynote.model.Product;
import com.ciny.studynote.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getProducts() throws SQLException {
        List<Product> products = productRepository.getProducts();

        return products;
    }

    public Product createProduct(ProductRequestDto requestDto) throws SQLException {
        Product product = new Product(requestDto);

        productRepository.createProduct(product);

        return product;
    }

    public Product updateProduct(Long id, ProductMypriceRequestDto requestDto) throws SQLException {
        Product product = productRepository.getProduct(id);

        if (product == null) {
            throw new NullPointerException("존재하지 않는 아이디입니다.");
        }

        int myprice = requestDto.getMyprice();
        productRepository.updateMyprice(id, myprice);

        product = productRepository.getProduct(id);
        return product;
    }
}
