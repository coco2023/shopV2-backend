package com.UmiUni.shop.component;

import com.UmiUni.shop.controller.SuppliersFinanceController;
import com.UmiUni.shop.model.FinancialReport;
import com.UmiUni.shop.service.SuppliersFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired
    private SuppliersFinanceService suppliersFinanceService;

    @Autowired
    private SuppliersFinanceController suppliersFinanceController;


    // Runs every day at 23:59:00 (11:59:00 PM)
    @Scheduled(cron = "0 59 23 * * ?")
    public void generateDailySalesReportAutomatically() {
        // method to get all supplier IDs for which reports should be generated
//        List<String> allSupplierIds = suppliersFinanceService.getAllSupplierIds();

        List<String> allSupplierIds = new ArrayList<>();
        allSupplierIds.add("3");

        // Current date in "yyyy/MM/dd" format
        String currentDateStr = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(LocalDate.now());

        for (String supplierId : allSupplierIds) {
            // Call the method to generate reports
            ResponseEntity<?> response = suppliersFinanceController.generateDailySalesReport(supplierId, currentDateStr);

            // Handle the response as necessary
        }
    }

    // Scheduled method for generating monthly reports
    // Runs at 23:59 on the last day of every month
    @Scheduled(cron = "0 59 23 L * ?")
    public void generateMonthlySalesReportAutomatically() {
//        List<String> allSupplierIds = suppliersFinanceService.getAllSupplierIds();
        List<String> allSupplierIds = new ArrayList<>();
        allSupplierIds.add("3");

        // Get the last day of the current month in "yyyy/MM" format
        YearMonth currentMonth = YearMonth.now();
        LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();
        String monthStr = DateTimeFormatter.ofPattern("yyyy/MM").format(lastDayOfMonth);

        for (String supplierId : allSupplierIds) {
            // Call the service method to generate the monthly report
            ResponseEntity<?> response = suppliersFinanceController.generateMonthlySalesReport(supplierId, monthStr);
            // Handle the report as necessary
        }
    }

}
