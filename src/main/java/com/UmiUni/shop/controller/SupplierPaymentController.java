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

    @Autowired
    private ControllerUtli controllerUtli;

    @GetMapping("/{supplierId}/payment/{id}")
    public ResponseEntity<PayPalPayment> getSupplierPaymentById(@PathVariable String supplierId, @PathVariable Long id) {
        return ResponseEntity.ok(supplierPaymentService.getPayPalPayment(supplierId, id));
    }
    @GetMapping("/payment/{id}")
    public ResponseEntity<PayPalPayment> getSupplierPaymentByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(supplierPaymentService.getPayPalPayment(String.valueOf(supplierId), id));
    }

    @GetMapping("/{supplierId}/all")
    public List<PayPalPayment> getSupplierAllPayments(@PathVariable String supplierId) {
        return supplierPaymentService.getAllPayPalPayment(supplierId);
    }
    @GetMapping("/all")
    public List<PayPalPayment> getSupplierAllPaymentsByToken(@RequestHeader("Authorization") String authorizationHeader) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return supplierPaymentService.getAllPayPalPayment(String.valueOf(supplierId));
    }

    @GetMapping("/{supplierId}/salesOrderSn/{salesOrderSn}")
    public List<PayPalPayment> getSupplierPaymentsBySupplierIdAndSalesOrderSn(@PathVariable String supplierId, @PathVariable String salesOrderSn) {
        return supplierPaymentService.getPayPalPaymentBySupplierIdAndSalesOrderSn(supplierId, salesOrderSn);
    }
    @GetMapping("/salesOrderSn/{salesOrderSn}")
    public List<PayPalPayment> getSupplierPaymentsBySupplierIdAndSalesOrderSnByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String salesOrderSn) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return supplierPaymentService.getPayPalPaymentBySupplierIdAndSalesOrderSn(String.valueOf(supplierId), salesOrderSn);
    }

    // fill the empty supplierId
    @GetMapping("/{supplierId}/salesOrderSn/all")
    public List<PayPalPayment> getSupplierPaymentsBySalesOrderSn(@PathVariable String supplierId) {
        return supplierPaymentService.getPayPalPaymentsBySalesOrderSn(supplierId);
    }
    @GetMapping("/salesOrderSn/all")
    public List<PayPalPayment> getSupplierPaymentsBySalesOrderSnByToken(@RequestHeader("Authorization") String authorizationHeader) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return supplierPaymentService.getPayPalPaymentsBySalesOrderSn(String.valueOf(supplierId));
    }

}
