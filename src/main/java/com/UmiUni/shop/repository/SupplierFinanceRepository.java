package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.SupplierFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface SupplierFinanceRepository extends JpaRepository<SupplierFinance, Long> {
    SupplierFinance findByReportDate(LocalDate startDate);
}
