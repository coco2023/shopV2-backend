package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.SupplierPayPalAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierPayPalAuthRepo extends JpaRepository<SupplierPayPalAuth, Long> {
}
