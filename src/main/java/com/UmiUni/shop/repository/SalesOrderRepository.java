package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> getSalesOrderBySalesOrderSn(String salesOrderSn);
}
