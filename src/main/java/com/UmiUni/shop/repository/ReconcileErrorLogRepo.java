package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.ReconcileErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReconcileErrorLogRepo extends JpaRepository<ReconcileErrorLog, Long> {
    Optional<ReconcileErrorLog> findBySalesOrderSn(String salesOrderId);
}
