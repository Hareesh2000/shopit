package com.shopit.project.repository;

import com.shopit.project.model.Category;
import com.shopit.project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductName(String productName);
    Page<Product> findByDeleteDateIsNull(Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCaseAndDeleteDateIsNull(String keyword, Pageable pageable);
    Page<Product> findByCategoryAndDeleteDateIsNull(Category category, Pageable pageable);
}
