package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrder;

import java.util.List;

public interface SalesOrderService {
    SalesOrder createSalesOrder(SalesOrder salesOrder);
    SalesOrder getSalesOrder(Long id);
    List<SalesOrder> getAllSalesOrders();
    SalesOrder updateSalesOrder(Long id, SalesOrder salesOrderDetails);
    void deleteSalesOrder(Long id);
}
