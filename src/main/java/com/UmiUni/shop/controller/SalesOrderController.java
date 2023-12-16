package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.service.SalesOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salesOrders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    public ResponseEntity<SalesOrder> createSalesOrder(@RequestBody SalesOrder salesOrder) {
        return ResponseEntity.ok(salesOrderService.createSalesOrder(salesOrder));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrder> getSalesOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrder(id));
    }

    @GetMapping("/salesOrderSn/{salesOrderSn}")
    public ResponseEntity<SalesOrder> getSalesOrderBySalesOrderSn(@PathVariable String salesOrderSn) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderBySalesOrderSn(salesOrderSn));
    }


    @GetMapping("/all")
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderService.getAllSalesOrders();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesOrder> updateSalesOrder(@PathVariable Long id, @RequestBody SalesOrder salesOrderDetails) {
        return ResponseEntity.ok(salesOrderService.updateSalesOrder(id, salesOrderDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalesOrder(@PathVariable Long id) {
        salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok().build();
    }
}
