package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.Payment;

import java.util.List;

public interface SupplierPaymentService {
    PayPalPayment getPayPalPayment(String supplierId, Long id);

    List<PayPalPayment> getAllPayPalPayment(String supplierId);

    List<PayPalPayment> getPayPalPaymentBySupplierIdAndSalesOrderSn(String supplierId, String salesOrderSn);

    List<PayPalPayment> getPayPalPaymentsBySalesOrderSn(String supplierId);
}
