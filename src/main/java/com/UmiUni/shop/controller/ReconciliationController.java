package com.UmiUni.shop.controller;

import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.model.PaypalTransactionRecord;
import com.UmiUni.shop.model.ReconcileOrderAndPayment;
import com.UmiUni.shop.model.ReconcileResult;
import com.UmiUni.shop.service.ReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reconciliation")
@Log4j2
public class ReconciliationController {

    @Autowired
    private ReconciliationService reconciliationService;

    // http://localhost:9001/api/v1/reconciliation/reconcile?salesOrderSn=SO-1702780660237-4055
    @GetMapping("/reconcile")
    public ResponseEntity<ReconcileOrderAndPayment> reconcilePayment(@RequestParam String salesOrderSn) {
        ReconcileOrderAndPayment result = reconciliationService.reconcilePaymentViaSalesOrderSn(salesOrderSn);
        return ResponseEntity.ok(result);
    }

    // http://localhost:9001/api/v1/reconciliation/reconcile/days-before/1
    @GetMapping("/reconcile/past-days/{days}")
    public ResponseEntity<List<ReconcileOrderAndPayment>> reconcilePastDays(@PathVariable("days") int days) {
        List<ReconcileOrderAndPayment> result = reconciliationService.reconcilePastDays(days);
        return ResponseEntity.ok(result);
    }

    // http://localhost:9001/api/v1/reconciliation/reconcile/between-days?start=2023/12/16&end=2023/12/17
    @GetMapping("/reconcile/between-days")
    public ResponseEntity<?> reconcileBetweenDates(@RequestParam String start, @RequestParam String end) {
        try {
            ArrayList<LocalDateTime> dates = convertStartAndEndDateFormat(start, end);
            LocalDateTime startDate = dates.get(0);
            LocalDateTime endDate = dates.get(1);
            List<ReconcileOrderAndPayment> result = reconciliationService.reconcileBetweenDates(startDate, endDate);
            return ResponseEntity.ok(result);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
        }
    }

    // http://localhost:9001/api/v1/reconciliation/monthly-sales-report?start=2023/12/10&end=2023/12/17&type=JSON
    @GetMapping("/monthly-sales-report")
    public ResponseEntity<?> getMonthlySalesReport(@RequestParam String start, @RequestParam String end, @RequestParam String type) {
        ByteArrayResource resource = null;
        File file = null;
        Path path = null;
        Map<LocalDate, DailyReport> reportMap = null;
        MediaType mediaType = null;

        try {
            ArrayList<LocalDateTime> dates = convertStartAndEndDateFormat(start, end);
            LocalDateTime startDate = dates.get(0);
            LocalDateTime endDate = dates.get(1);

            reportMap = reconciliationService.generateMonthlySalesReport(startDate, endDate, type);

            if ( type.equals("JSON")) {
                file = generateJsonFile(reportMap);
                // Return the report as a file download or as JSON data
                path = file.toPath();
                mediaType = MediaType.APPLICATION_JSON;
            } else {
                // csv
                file = generateCsvFile(reportMap);
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            // Return the report as a file download or as JSON data
            path = file.toPath();
            resource = new ByteArrayResource(Files.readAllBytes(path));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("IOException: " + e.getMessage());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * upload paypal csv file to parse
     */
    @PostMapping("/upload")
    public ResponseEntity<List<?>> uploadFile(@RequestParam("file") MultipartFile file) {
        List<ReconcileResult> reconcileResults = reconciliationService.readTransactions(file);
        log.info("file: " + reconcileResults);
        return ResponseEntity.ok(reconcileResults);
    }

    private ArrayList<LocalDateTime> convertStartAndEndDateFormat(String start, String end) {

        ArrayList<LocalDateTime> dates = new ArrayList<>();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate startDateTime = LocalDate.parse(start, formatter);
            LocalDate endDateTime = LocalDate.parse(end, formatter);

            LocalDateTime startDate = startDateTime.atStartOfDay();
            LocalDateTime endDate = endDateTime.atTime(23, 59, 59);
            dates.add(startDate);
            dates.add(endDate);
        }  catch (DateTimeParseException e) {
            log.error("Invalid date format: " + e.getMessage());
        }
        return dates;
    }

    private File generateJsonFile(Map<LocalDate, DailyReport> reportMap) {
        // Define the path to the output directory
        String outputDir = Paths.get("doc", "report").toString();
        File jsonOutputFile = new File(outputDir, "monthly_sales_report.json");

        // Use Jackson's ObjectMapper to write the map as JSON to the file
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Convert map to JSON and write to the file
            mapper.writeValue(jsonOutputFile, reportMap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonOutputFile;
    }

    private File generateCsvFile(Map<LocalDate, DailyReport> reportMap) {
        // Create a temporary file to write the report to
        // Define the path to the resources directory
        String outputDir = Paths.get("doc", "report").toString();

        File csvOutputFile = new File(outputDir,"monthly_sales_report.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            // write the header line
            pw.println("Date,Payments Received,Amount Received,Fees,Net Amount");

            // Write each line of the report
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            reportMap.forEach((date, report) -> {
                String line = String.join(",",
                        dateFormatter.format(date),
                        String.valueOf(report.getPaymentsReceived()),
                        report.getTotalAmountReceived().toString(),
                        report.getFees().toString(),
                        report.getNetAmount().toString()
                );
                pw.println(line);
            });

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // Return the path to the generated file
        return csvOutputFile;
    }

}
