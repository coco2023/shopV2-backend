package com.UmiUni.shop.repository;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> getSalesOrderBySalesOrderSn(String salesOrderSn);

    List<SalesOrder> getSalesOrdersByOrderDateAfterAndOrderStatus(LocalDateTime day, OrderStatus orderStatus);

    List<SalesOrder> findByOrderDateBetweenAndOrderStatus(LocalDateTime startDate, LocalDateTime endDate, OrderStatus orderStatus);
}
