package com.ciny.studynote.repository;

import com.ciny.studynote.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}