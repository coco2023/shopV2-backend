package com.UmiUni.shop.service;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.SalesOrder;

import java.util.List;

public interface SalesOrderService {
    SalesOrder createSalesOrder(SalesOrder salesOrder);
    SalesOrder getSalesOrder(Long id);
    List<SalesOrder> getAllSalesOrders();
    SalesOrder updateSalesOrder(Long id, SalesOrder salesOrderDetails);
    void deleteSalesOrder(Long id);

    SalesOrder getSalesOrderBySalesOrderSn(String salesOrderSn);

    boolean canCancelOrder(String salesOrderSn);

    void cancelOrder(String salesOrderSn);

    void updateOrderStatusBySalesOrderSn(String salesOrderSn, OrderStatus name);
}
