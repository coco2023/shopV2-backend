package com.UmiUni.shop.controller;

import com.UmiUni.shop.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {

    @Autowired
    private ReconciliationService reconciliationService;

    @GetMapping("/reconcile")
    public ResponseEntity<String> reconcilePayment(@RequestParam String salesOrderSn) {
        try {
            String result = reconciliationService.reconcilePaymentViaSalesOrderSn(salesOrderSn);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Log the exception details
            return ResponseEntity.internalServerError().body("Reconciliation failed: " + e.getMessage());
        }
    }

    @GetMapping("/reconcile/days-before/{days}")
    public ResponseEntity<String> reconcilePastDays(@PathVariable("days") int days) {
        try {
            String result = reconciliationService.reconcilePastDays(days);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Log the exception details
            return ResponseEntity.internalServerError().body("Reconciliation failed: " + e.getMessage());
        }
    }

}
