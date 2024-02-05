package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.OrderNotFoundException;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.SalesOrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class SalesOrderServiceImpl implements SalesOrderService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Override
    public SalesOrder createSalesOrder(SalesOrder salesOrder) {
        LocalDateTime time = LocalDateTime.now();
        salesOrder.setOrderDate(time);
        salesOrder.setLastUpdated(time);
        salesOrder.setExpirationDate(time.plusMinutes(2));
        log.info("***salesOrder: " + salesOrder);
        return salesOrderRepository.save(salesOrder);
    }

    @Override
    public SalesOrder getSalesOrder(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found with id: " + id));
    }

    @Override
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    @Override
    public SalesOrder updateSalesOrder(Long id, SalesOrder salesOrderDetails) {
        SalesOrder salesOrder = getSalesOrder(id);
        salesOrder.setSalesOrderSn(salesOrderDetails.getSalesOrderSn());
        salesOrder.setCustomerId(salesOrderDetails.getCustomerId());
        salesOrder.setOrderDate(salesOrderDetails.getOrderDate());
        salesOrder.setTotalAmount(salesOrderDetails.getTotalAmount());
        salesOrder.setShippingAddress(salesOrderDetails.getShippingAddress());
        salesOrder.setBillingAddress(salesOrderDetails.getBillingAddress());
        salesOrder.setOrderStatus(salesOrderDetails.getOrderStatus());
        salesOrder.setPaymentMethod(salesOrderDetails.getPaymentMethod());
        // other updates as needed
        return salesOrderRepository.save(salesOrder);
    }

    @Override
    public void deleteSalesOrder(Long id) {
        salesOrderRepository.deleteById(id);
    }

    @Override
    public SalesOrder getSalesOrderBySalesOrderSn(String salesOrderSn) {
        return salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
    }

    @Override
    public boolean canCancelOrder(String salesOrderSn) {
        SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
        if (salesOrder == null) {
            throw new OrderNotFoundException("Order with SN " + salesOrderSn + " not found.");
        }

        // Check if the order's status allows for cancellation
        return salesOrder.getOrderStatus().equals(OrderStatus.PENDING) || salesOrder.getOrderStatus().equals(OrderStatus.PROCESSING);
    }

    @Override
    public void cancelOrder(String salesOrderSn) {
        SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn).get();
        if (salesOrder == null) {
            throw new OrderNotFoundException("Order with SN " + salesOrderSn + " not found.");
        }

        // Update order status
        salesOrder.setOrderStatus(OrderStatus.CANCELLED);
        salesOrder.setLastUpdated(LocalDateTime.now());
        salesOrderRepository.save(salesOrder);

        // Additional logic might include:
        // TODO: - Issuing a refund if the order was paid
        // TODO: - Updating inventory to reflect the cancellation
        // TODO: - Notifying the customer of the cancellation

    }
}
