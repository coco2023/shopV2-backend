package com.UmiUni.shop.controller;

import com.UmiUni.shop.model.FinancialReport;
import com.UmiUni.shop.service.SuppliersFinanceService;
import com.UmiUni.shop.utils.DatesFormatConvert;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers/finance")
@Log4j2
public class SuppliersFinanceController {

    @Autowired
    private DatesFormatConvert datesFormatConvert;
    // Constructor to inject the dependency

    @Autowired
    private SuppliersFinanceService suppliersFinanceService;

    // generate years, get +1 years...
//    @GetMapping("/{supplierId}/yearly-financial-report")
//    public ResponseEntity<?> getYearlySalesReport(@PathVariable String supplierId, @RequestParam String start, @RequestParam String end, @RequestParam String type) {
//        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDateFormat(start, null, type);
//
//        FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1));
//        log.info("financialReport: " + financialReport);
//        return ResponseEntity.ok(financialReport);
//    }
//
//    // generate months report, get +1 months...
//    @GetMapping("/{supplierId}/monthly-financial-report")
//    public ResponseEntity<?> getMonthlySalesReport(@PathVariable String supplierId, @RequestParam String start, @RequestParam String end, @RequestParam String type) {
//        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDateFormat(start, null, type);
//
//        FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1));
//        log.info("financialReport: " + financialReport);
//        return ResponseEntity.ok(financialReport);
//    }

    // type: daily, monthly, yearly
    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report?date=2023/12/16&type=daily
    @GetMapping("/{supplierId}/financial-report/day-by-day")
    public ResponseEntity<?> getDailySalesReport(@PathVariable String supplierId, @RequestParam String date, @RequestParam String type) {
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDateFormat(date, type);

        FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1));
        log.info("financialReport: " + financialReport);
        return ResponseEntity.ok(financialReport);
    }

    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report?date=2023/12/15&type=yearly
    @GetMapping("/{supplierId}/financial-report")
    public ResponseEntity<List<FinancialReport>> getMonthlySalesReport(@PathVariable String supplierId, @RequestParam String date, @RequestParam String type) {
        log.info("date: " + date);
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDateFormat(date, type);

        if (type.equals("monthly")) {
            // monthly: start date 2023-12-01T00:00 and end date 2024-01-01T00:00
            List<FinancialReport> monthlyFinancialReports = new ArrayList<>();
            for (LocalDateTime day = dates.get(0); !day.isAfter(dates.get(1)); day = day.plusDays(1)) {
//                log.info("start date {} ; end date {}", day, day.plusDays(1));
                FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReport(
                        Long.valueOf(supplierId),
                        day,
                        day.plusDays(1)
                );
                monthlyFinancialReports.add(financialReport);
            }
//            log.info("financialReport: " + monthlyFinancialReports);
            return ResponseEntity.ok(monthlyFinancialReports);
        }

        if (type.equals("yearly")) {
            List<FinancialReport> yearlyFinancialReports = new ArrayList<>();
            for (LocalDateTime startMonth = dates.get(0); !startMonth.isAfter(dates.get(1)); startMonth = startMonth.with(TemporalAdjusters.firstDayOfNextMonth())) {
//                log.info("start month {} ; end month {}", startMonth, startMonth.with(TemporalAdjusters.firstDayOfNextMonth()));
                FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReport(
                        Long.valueOf(supplierId),
                        startMonth,
                        startMonth.with(TemporalAdjusters.firstDayOfNextMonth())
                );
                yearlyFinancialReports.add(financialReport);
            }
//            log.info("financialReport: " + yearlyFinancialReports);
            return ResponseEntity.ok(yearlyFinancialReports);
        }
        return null;
    }

}
