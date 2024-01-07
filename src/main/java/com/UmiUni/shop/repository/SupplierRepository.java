package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByPaypalEmail(String email);

    boolean existsBySupplierName(String username);

    boolean existsByContactInfo(String email);

    Optional<Supplier> findBySupplierName(String username);
}
