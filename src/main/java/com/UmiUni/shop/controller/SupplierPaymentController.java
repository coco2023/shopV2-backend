package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.service.PayPalService;
import com.UmiUni.shop.service.PaymentService;
import com.UmiUni.shop.service.StripeService;
import com.UmiUni.shop.service.SupplierPaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PayPal Payments
 */
@RestController
@RequestMapping("/api/v1/suppliers/payments")
@Log4j2
public class SupplierPaymentController {

    @Autowired
    private SupplierPaymentService supplierPaymentService;

    @GetMapping("/{supplierId}/payment/{id}")
    public ResponseEntity<PayPalPayment> getSupplierPaymentById(@PathVariable String supplierId, @PathVariable Long id) {
        return ResponseEntity.ok(supplierPaymentService.getPayPalPayment(supplierId, id));
    }

    @GetMapping("/{supplierId}/all")
    public List<PayPalPayment> getSupplierAllPayments(@PathVariable String supplierId) {
        return supplierPaymentService.getAllPayPalPayment(supplierId);
    }

    @GetMapping("/{supplierId}/salesOrderSn/{salesOrderSn}")
    public List<PayPalPayment> getSupplierPaymentsBySupplierIdAndSalesOrderSn(@PathVariable String supplierId, @PathVariable String salesOrderSn) {
        return supplierPaymentService.getPayPalPaymentBySupplierIdAndSalesOrderSn(supplierId, salesOrderSn);
    }

    // fill the empty supplierId
    @GetMapping("/{supplierId}/salesOrderSn/all")
    public List<PayPalPayment> getSupplierPaymentsBySalesOrderSn(@PathVariable String supplierId) {
        return supplierPaymentService.getPayPalPaymentsBySalesOrderSn(supplierId);
    }

}
