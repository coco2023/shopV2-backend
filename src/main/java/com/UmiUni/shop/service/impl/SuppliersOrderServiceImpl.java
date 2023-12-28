package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.SuppliersOrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class SuppliersOrderServiceImpl implements SuppliersOrderService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Override
    public SalesOrder getSuppliersSalesOrder(Long supplierId, Long id) {
        return salesOrderRepository.findBySupplierIdAndSalesOrderId(supplierId, id)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found with id: " + id));
    }

    @Override
    public SalesOrder getSuppliersSalesOrderBySalesOrderSn(Long supplierId, String salesOrderSn) {
        return salesOrderRepository.findBySupplierIdAndSalesOrderSn(supplierId, salesOrderSn)
                .orElseThrow(() -> new RuntimeException("supplierId/ salesOrderSn not exit!"));}

    @Override
    public List<SalesOrder> getSuppliersAllSalesOrders(Long supplierId) {
        return salesOrderRepository.findAllBySupplierId(supplierId);
    }

    @Override
    public SalesOrder updateSuppliersSalesOrder(Long supplierId, Long id, SalesOrder salesOrderDetails) {
        SalesOrder salesOrder = getSuppliersSalesOrder(supplierId, id);
        salesOrder.setSalesOrderSn(salesOrderDetails.getSalesOrderSn());
        salesOrder.setCustomerId(salesOrderDetails.getCustomerId());
        salesOrder.setOrderDate(salesOrderDetails.getOrderDate());
        salesOrder.setTotalAmount(salesOrderDetails.getTotalAmount());
        salesOrder.setShippingAddress(salesOrderDetails.getShippingAddress());
        salesOrder.setBillingAddress(salesOrderDetails.getBillingAddress());
        salesOrder.setOrderStatus(salesOrderDetails.getOrderStatus());
        salesOrder.setPaymentMethod(salesOrderDetails.getPaymentMethod());
        return salesOrderRepository.save(salesOrder);
    }

    @Override
    public void deleteSuppliersSalesOrder(Long supplierId, Long id) {
        salesOrderRepository.deleteBySalesOrderIdAndSupplierId(supplierId, id);
    }
}
