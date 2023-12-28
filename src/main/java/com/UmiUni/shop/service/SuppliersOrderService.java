package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrder;

import java.util.List;

public interface SuppliersOrderService {
    SalesOrder getSuppliersSalesOrder(Long supplierId, Long id);

    SalesOrder getSuppliersSalesOrderBySalesOrderSn(Long supplierId, String salesOrderSn);

    List<SalesOrder> getSuppliersAllSalesOrders(Long supplierId);

    SalesOrder updateSuppliersSalesOrder(Long supplierId, Long id, SalesOrder salesOrderDetails);

    void deleteSuppliersSalesOrder(Long supplierId, Long id);
}
