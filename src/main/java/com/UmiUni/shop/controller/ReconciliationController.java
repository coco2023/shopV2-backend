package com.UmiUni.shop.controller;

import com.UmiUni.shop.service.ReconciliationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Log4j2
public class ReconciliationController {

    @Autowired
    private ReconciliationService reconciliationService;

    // http://localhost:9001/api/v1/reconciliation/reconcile?salesOrderSn=SO-1702780660237-4055
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

    // http://localhost:9001/api/v1/reconciliation/reconcile/days-before/1
    @GetMapping("/reconcile/past-days/{days}")
    public ResponseEntity<String> reconcilePastDays(@PathVariable("days") int days) {
        try {
            String result = reconciliationService.reconcilePastDays(days);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Log the exception details
            return ResponseEntity.internalServerError().body("Reconciliation failed: " + e.getMessage());
        }
    }

    // http://localhost:9001/api/v1/reconciliation/reconcile/between-days?start=2023/12/16&end=2023/12/17
    @GetMapping("/reconcile/between-days")
    public ResponseEntity<String> reconcileBetweenDates(@RequestParam String start, @RequestParam String end) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate startDateTime  = LocalDate.parse(start, formatter);
            LocalDate endDateTime  = LocalDate.parse(end, formatter);

            LocalDateTime startDate = startDateTime.atStartOfDay();
            LocalDateTime endDate = endDateTime.atTime(23, 59, 59);

            String result = reconciliationService.reconcileBetweenDates(startDate, endDate);
            return ResponseEntity.ok(result);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Reconciliation failed: " + e.getMessage());
        }
    }

    // http://localhost:9001/api/v1/reconciliation/monthly-sales-report?start=2023/12/16&end=2023/12/17
    @GetMapping("/monthly-sales-report")
    public ResponseEntity<?> getMonthlySalesReport(@RequestParam String start, @RequestParam String end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate startDateTime  = LocalDate.parse(start, formatter);
        LocalDate endDateTime  = LocalDate.parse(end, formatter);

        LocalDateTime startDate = startDateTime.atStartOfDay();
        LocalDateTime endDate = endDateTime.atTime(23, 59, 59);

        File report = reconciliationService.generateMonthlySalesReport(startDate, endDate);
        // Return the report as a file download or as JSON data
        return ResponseEntity.ok(report);
    }

}
