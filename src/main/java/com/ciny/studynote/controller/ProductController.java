package com.ciny.studynote.controller;

import com.ciny.studynote.dto.ProductMypriceRequestDto;
import com.ciny.studynote.dto.ProductRequestDto;
import com.ciny.studynote.global.exception.RestApiException;
import com.ciny.studynote.model.Product;
import com.ciny.studynote.model.UserRoleEnum;
import com.ciny.studynote.security.UserDetailsImpl;
import com.ciny.studynote.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductService productService;

    // 즐겨찾기 상품 전체 조회
    @GetMapping("/api/products")
    public ResponseEntity getProducts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        try {
            return ResponseEntity.ok(productService.getProducts(userId));
        } catch (AccessDeniedException ex) {
            return new ResponseEntity(RestApiException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .errorMessage(ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // 즐겨찾기에 추가
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        return productService.createProduct(requestDto, userId);
    }

    // 관심가격 설정하기
    @PutMapping("/api/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        return productService.updateProduct(id, userId, requestDto);
    }

    // 관리자용 상품 조회하기
    @Secured(UserRoleEnum.Authority.ADMIN)
    @GetMapping("/api/admin/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
}
