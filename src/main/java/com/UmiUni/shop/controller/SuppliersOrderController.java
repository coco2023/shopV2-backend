package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.service.SalesOrderService;
import com.UmiUni.shop.service.SuppliersOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers/salesOrders")
public class SuppliersOrderController {

    @Autowired
    private SuppliersOrderService salesOrderService;

    @GetMapping("/{supplierId}/{id}")
    public ResponseEntity<SalesOrder> getSalesOrderById(@PathVariable Long supplierId, @PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSuppliersSalesOrder(supplierId, id));
    }

    @GetMapping("/{supplierId}/salesOrderSn/{salesOrderSn}")
    public ResponseEntity<SalesOrder> getSalesOrderBySalesOrderSn(@PathVariable Long supplierId, @PathVariable String salesOrderSn) {
        return ResponseEntity.ok(salesOrderService.getSuppliersSalesOrderBySalesOrderSn(supplierId, salesOrderSn));
    }


    @GetMapping("/{supplierId}/all")
    public List<SalesOrder> getAllSalesOrders(@PathVariable Long supplierId) {
        return salesOrderService.getSuppliersAllSalesOrders(supplierId);
    }

    @PutMapping("/{supplierId}/{id}")
    public ResponseEntity<SalesOrder> updateSalesOrder(@PathVariable Long supplierId, @PathVariable Long id, @RequestBody SalesOrder salesOrderDetails) {
        return ResponseEntity.ok(salesOrderService.updateSuppliersSalesOrder(supplierId, id, salesOrderDetails));
    }

    @DeleteMapping("/{supplierId}/{id}")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long supplierId, @PathVariable Long id) {
        salesOrderService.deleteSuppliersSalesOrder(supplierId, id);
        return ResponseEntity.ok().build();
    }

}
