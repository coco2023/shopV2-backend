package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.SalesOrderDetail;
import com.UmiUni.shop.service.SalesOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/salesOrderDetails")
public class SalesOrderDetailController {

    @Autowired
    private SalesOrderDetailService salesOrderDetailService;

    @PostMapping
    public ResponseEntity<SalesOrderDetail> createSalesOrderDetail(@RequestBody SalesOrderDetail salesOrderDetail) {
        return ResponseEntity.ok(salesOrderDetailService.createSalesOrderDetail(salesOrderDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderDetail> getSalesOrderDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderDetailService.getSalesOrderDetail(id));
    }

    @GetMapping("/all")
    public List<SalesOrderDetail> getAllSalesOrderDetails() {
        return salesOrderDetailService.getAllSalesOrderDetails();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesOrderDetail> updateSalesOrderDetail(@PathVariable Long id, @RequestBody SalesOrderDetail salesOrderDetailDetails) {
        return ResponseEntity.ok(salesOrderDetailService.updateSalesOrderDetail(id, salesOrderDetailDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalesOrderDetail(@PathVariable Long id) {
        salesOrderDetailService.deleteSalesOrderDetail(id);
        return ResponseEntity.ok().build();
    }
}
