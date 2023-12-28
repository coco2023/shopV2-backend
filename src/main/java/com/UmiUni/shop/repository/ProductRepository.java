package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuCode(String skuCode);

    Optional<Product> findByProductIdAndSupplierId(Long id, Long supplierId);

    Optional<List<Product>> findAllBySupplierId(Long supplierId);

    void deleteByProductIdAndSupplierId(Long id, Long supplierId);

    Product findBySkuCodeAndSupplierId(String skuCode, Long supplierId);
}
