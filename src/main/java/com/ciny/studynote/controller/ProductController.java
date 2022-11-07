package com.ciny.studynote.controller;

import com.ciny.studynote.dto.ProductMypriceRequestDto;
import com.ciny.studynote.dto.ProductRequestDto;
import com.ciny.studynote.model.Product;
import com.ciny.studynote.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductService productService;

    // 즐겨찾기 상품 전체 조회
    @GetMapping("/api/products")
    public List<Product> getProducts() throws SQLException {
        return productService.getProducts();
    }

    // 즐겨찾기에 추가
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto) throws SQLException {
        return productService.createProduct(requestDto);
    }

    // 관심가격 설정하기
    @PutMapping("/api/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) throws SQLException {
        return productService.updateProduct(id, requestDto);
    }
}
