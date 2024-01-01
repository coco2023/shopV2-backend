package com.UmiUni.shop.controller;

import com.UmiUni.shop.constant.ReportType;
import com.UmiUni.shop.entity.SupplierFinance;
import com.UmiUni.shop.model.FinancialReport;
import com.UmiUni.shop.repository.SupplierFinanceRepository;
import com.UmiUni.shop.service.SuppliersFinanceService;
import com.UmiUni.shop.utils.DatesFormatConvert;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private SupplierFinanceRepository supplierFinanceRepository;

    /**
     * get report
     */
    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report/get?time=2023/12&type=monthly
    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report/get?time=2023&type=yearly
    @GetMapping("/{supplierId}/financial-report/get")
    public ResponseEntity<?> getSalesReport(@PathVariable String supplierId, @RequestParam String time, @RequestParam String type){
        if (type.equals("monthly")) {
            // convert the date format
            List<LocalDateTime> dates = datesFormatConvert.convertFinancialMonthFormat(time);
            String startDate = String.valueOf(LocalDate.from(dates.get(0)));
            LocalDate end = LocalDate.from(dates.get(1));
            if (end.isAfter(LocalDate.now())) {
                end = LocalDate.now();
            }
            String endDate = String.valueOf(end);

            List<SupplierFinance> supplierFinanceList1 = suppliersFinanceService.getMonthlySalesReport(Long.valueOf(supplierId), startDate, endDate, ReportType.DAILY);
            return ResponseEntity.ok(supplierFinanceList1);

        } else if (type.equals("yearly")) {
            List<LocalDateTime> dates = datesFormatConvert.convertFinancialYearFormat(time);
            // get monthly supplierFinance in db between start - end
            String start = dates.get(0).getYear() + "-" + dates.get(0).getMonth().getValue();
            String end = dates.get(1).getYear() + "-" + dates.get(1).getMonth().getValue();

            List<SupplierFinance> supplierFinanceList1 = suppliersFinanceService.getYearlySalesReport(Long.valueOf(supplierId), start, end, ReportType.MONTHLY);
            return ResponseEntity.ok(supplierFinanceList1);
        }
        return null;
    }

    /**
     * generate report
     */
    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report/generate?daily=2023/12/29
    @GetMapping("/{supplierId}/financial-report/day")
    public ResponseEntity<?> generateDailySalesReport(@PathVariable String supplierId, @RequestParam String daily) {
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDayFormat(daily);

        FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1), ReportType.DAILY);
        log.info("financialReport: " + financialReport);
        return ResponseEntity.ok(financialReport);
    }

    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report/generate?month=2023/12
    @GetMapping("/{supplierId}/financial-report/month")
    public ResponseEntity<?> generateMonthlySalesReport(@PathVariable String supplierId, @RequestParam String month) {
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialMonthFormat(month);

        FinancialReport financialReport = suppliersFinanceService.generateMonthlyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1), ReportType.MONTHLY);
        log.info("financialReport: " + financialReport);
        return ResponseEntity.ok(financialReport);
    }

    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report/generate?year=2023
    @GetMapping("/{supplierId}/financial-report/year")
    public ResponseEntity<?> generateYearlySalesReport(@PathVariable String supplierId, @RequestParam String year) {
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialYearFormat(year);

        FinancialReport financialReport = suppliersFinanceService.generateYearlyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1), ReportType.YEARLY);
        log.info("financialReport: " + financialReport);
        return ResponseEntity.ok(financialReport);
    }

    /**
     * v1
     * @param supplierId
     * @param date
     * @param type
     * @return
     */
    // type: daily, monthly, yearly
    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report?date=2023/12/16&type=daily
    @GetMapping("/{supplierId}/financial-report/day-by-day")
    public ResponseEntity<?> getDailySalesReport(@PathVariable String supplierId, @RequestParam String date, @RequestParam String type) {
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDateFormat(date, type);

        FinancialReport financialReport = suppliersFinanceService.autoGenerateDailyFinancialReport(Long.valueOf(supplierId), dates.get(0), dates.get(1));
        log.info("financialReport: " + financialReport);
        return ResponseEntity.ok(financialReport);
    }

    // http://localhost:9001/api/v1/suppliers/finance/3/financial-report?date=2023/12&type=yearly
    @GetMapping("/{supplierId}/financial-report")
    public ResponseEntity<List<FinancialReport>> getMonthlySalesReport(@PathVariable String supplierId, @RequestParam String date, @RequestParam String type) {
        log.info("date: " + date);
//        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDateFormat(date, type);
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialMonthFormat(date);  // "yyyy/MM"

        if (type.equals("monthly")) {
            // monthly: start date 2023-12-01T00:00 and end date 2024-01-01T00:00
            List<FinancialReport> monthlyFinancialReports = new ArrayList<>();
            for (LocalDateTime day = dates.get(0); !day.isAfter(LocalDateTime.now()); day = day.plusDays(1)) {
//                log.info("start date {} ; end date {}", day, day.plusDays(1));
                FinancialReport financialReport = suppliersFinanceService.autoGenerateDailyFinancialReport(
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
            for (LocalDateTime startMonth = dates.get(0); !startMonth.isAfter(LocalDateTime.now()); startMonth = startMonth.with(TemporalAdjusters.firstDayOfNextMonth())) {
//                log.info("start month {} ; end month {}", startMonth, startMonth.with(TemporalAdjusters.firstDayOfNextMonth()));
                FinancialReport financialReport = suppliersFinanceService.autoGenerateDailyFinancialReport(
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
