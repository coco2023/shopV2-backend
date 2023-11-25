package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.SalesOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long> {
}
