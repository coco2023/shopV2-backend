package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrderDetail;

import java.util.List;

public interface SalesOrderDetailService {
    SalesOrderDetail createSalesOrderDetail(SalesOrderDetail salesOrderDetail);
    SalesOrderDetail getSalesOrderDetail(Long id);
    List<SalesOrderDetail> getAllSalesOrderDetails();
    SalesOrderDetail updateSalesOrderDetail(Long id, SalesOrderDetail salesOrderDetailDetails);
    void deleteSalesOrderDetail(Long id);
}
