package com.UmiUni.shop.repository;

import com.UmiUni.shop.constant.ReportType;
import com.UmiUni.shop.entity.SupplierFinance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierFinanceRepository extends JpaRepository<SupplierFinance, Long> {
    Optional<SupplierFinance> findBySupplierIdAndReportDate(Long supplierId, String startDate);

    List<SupplierFinance> findBySupplierIdAndReportDateBetween(Long supplierId, String startDate, String endDate);

    Optional<SupplierFinance> findBySupplierIdAndReportDateAndReportType(Long supplierId, String reportDateString, ReportType reportType);

    List<SupplierFinance> findAllBySupplierIdAndReportTypeAndReportDateBetween(Long supplierId, ReportType reportType, String time, String time1);
}
