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

    @Autowired
    private ControllerUtli controllerUtli;

    @GetMapping("/{supplierId}/{id}")
    public ResponseEntity<SalesOrder> getSalesOrderById(@PathVariable Long supplierId, @PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSuppliersSalesOrder(supplierId, id));
    }
    @GetMapping("/{id}")
    public ResponseEntity<SalesOrder> getSalesOrderByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(salesOrderService.getSuppliersSalesOrder(supplierId, id));
    }

    @GetMapping("/{supplierId}/salesOrderSn/{salesOrderSn}")
    public ResponseEntity<SalesOrder> getSalesOrderBySalesOrderSn(@PathVariable Long supplierId, @PathVariable String salesOrderSn) {
        return ResponseEntity.ok(salesOrderService.getSuppliersSalesOrderBySalesOrderSn(supplierId, salesOrderSn));
    }
    @GetMapping("/salesOrderSn/{salesOrderSn}")
    public ResponseEntity<SalesOrder> getSalesOrderBySalesOrderSnAndToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String salesOrderSn) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(salesOrderService.getSuppliersSalesOrderBySalesOrderSn(supplierId, salesOrderSn));
    }

    // get all
    @GetMapping("/{supplierId}/all")
    public List<SalesOrder> getAllSalesOrders(@PathVariable Long supplierId) {
        return salesOrderService.getSuppliersAllSalesOrders(supplierId);
    }
    @GetMapping("/all")
    public List<SalesOrder> getAllSalesOrdersByToken(@RequestHeader("Authorization") String authorizationHeader) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return salesOrderService.getSuppliersAllSalesOrders(supplierId);
    }

    @PutMapping("/{supplierId}/{id}")
    public ResponseEntity<SalesOrder> updateSalesOrder(@PathVariable Long supplierId, @PathVariable Long id, @RequestBody SalesOrder salesOrderDetails) {
        return ResponseEntity.ok(salesOrderService.updateSuppliersSalesOrder(supplierId, id, salesOrderDetails));
    }
    @PutMapping("/{id}")
    public ResponseEntity<SalesOrder> updateSalesOrderByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id, @RequestBody SalesOrder salesOrderDetails) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(salesOrderService.updateSuppliersSalesOrder(supplierId, id, salesOrderDetails));
    }

    @DeleteMapping("/{supplierId}/{id}")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long supplierId, @PathVariable Long id) {
        salesOrderService.deleteSuppliersSalesOrder(supplierId, id);
        return ResponseEntity.ok().build();
    }
    // TODO: does not work
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalesOrderByToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id) {
        Long supplierId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        salesOrderService.deleteSuppliersSalesOrder(supplierId, id);
        return ResponseEntity.ok().build();
    }

}
