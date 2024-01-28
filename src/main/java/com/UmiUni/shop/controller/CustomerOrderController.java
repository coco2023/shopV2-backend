package com.UmiUni.shop.controller;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.service.SuppliersOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/salesOrders")
public class CustomerOrderController {

    @Autowired
    private SuppliersOrderService salesOrderService;

    @Autowired
    private ControllerUtli controllerUtli;

    // get customers' all salesOrder
    // http://localhost:9001/api/v1/customers/salesOrders/all
    @GetMapping("/all")
    public List<SalesOrder> getAllSalesOrdersByToken(@RequestHeader("Authorization") String authorizationHeader) {
        Long customerId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return salesOrderService.getCustomersAllSalesOrders(customerId);
    }

    // get salesOrder by salesOrderSn
    // http://localhost:9001/api/v1/customers/salesOrders/salesOrderSn/SO-1700799276864-6317
    @GetMapping("/salesOrderSn/{salesOrderSn}")
    public ResponseEntity<SalesOrder> getSalesOrderBySalesOrderSnAndToken(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String salesOrderSn) {
        Long customerId = controllerUtli.getSupplierIdByToken(authorizationHeader);
        return ResponseEntity.ok(salesOrderService.getCustomersSalesOrderBySalesOrderSn(customerId, salesOrderSn));
    }

}
