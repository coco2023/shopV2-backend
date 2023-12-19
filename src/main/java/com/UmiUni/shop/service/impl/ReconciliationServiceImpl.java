package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.ReconcileErrorLog;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.DBPaymentNotExitException;
import com.UmiUni.shop.exception.PaymentRecordNotMatchException;
import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.model.PaypalTransactionRecord;
import com.UmiUni.shop.model.ReconcileResult;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.ReconcileErrorLogRepo;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.ReconcileErrorLogService;
import com.UmiUni.shop.service.ReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Log4j2
public class ReconciliationServiceImpl implements ReconciliationService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

    @Autowired
    private ReconcileErrorLogService reconcileErrorLogService;

    public String reconcilePaymentViaSalesOrderSn(String salesOrderSn) {
        Optional<SalesOrder> salesOrderOpt = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn);
        Optional<PayPalPayment> payPalPaymentOpt = payPalPaymentRepository.findBySalesOrderSn(salesOrderSn);

        if( salesOrderOpt.isPresent() && payPalPaymentOpt.isPresent() ) {
            SalesOrder salesOrder = salesOrderOpt.get();
            PayPalPayment payPalPayment = payPalPaymentOpt.get();
            log.info("salesOrder: " + salesOrder.getOrderStatus() + " payPalPayment: " + payPalPayment.getPaymentState());

            if (isReconciliationSuccessful(salesOrder, payPalPayment)) {
                return "Reconciliation successful";
            } else {
                // Handle or log the error
                return "Reconciliation failed";
            }
        } else {
            return "SalesOrder or PayPalPayment not found";
        }

    }

    @Override
    public String reconcilePastDays(int days) {

        LocalDateTime daysBefore = LocalDateTime.now().minusDays(days);
        log.info("daysBefore: " + daysBefore);
        List<SalesOrder> recentSalesOrders = salesOrderRepository.getSalesOrdersByOrderDateAfterAndOrderStatus(daysBefore, OrderStatus.PROCESSING);
        List<PayPalPayment> recentPayments = payPalPaymentRepository.getPayPalPaymentsByCreateTimeAfterAndPaymentState(daysBefore, "complete");
//        log.info("recentSalesOrders: " + recentSalesOrders);
//        log.info("recentPayments: " + recentPayments);

        for (SalesOrder order : recentSalesOrders) {
            for (PayPalPayment payment : recentPayments) {
                    isReconciliationSuccessful(order, payment);
            }
        }
        return "Reconciliation for past days completed!";
    }

    @Override
    public String reconcileBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {

        //get all the order & payment records between these days with valid status
        List<SalesOrder> salesOrders = salesOrderRepository.findByOrderDateBetweenAndOrderStatus(startDate, endDate, OrderStatus.PROCESSING);
        List<PayPalPayment> payPalPayments = payPalPaymentRepository.findByCreateTimeBetweenAndPaymentState(startDate, endDate, "complete");

        for (SalesOrder order : salesOrders) {
            for (PayPalPayment payment : payPalPayments) {
                isReconciliationSuccessful(order, payment);
            }
        }
        return "Reconciliation from " + startDate + " to " + endDate + " is completed!";
    }

    @Override
    public File generateMonthlySalesReport(LocalDateTime startDate, LocalDateTime endDate, String type) {

        // get the payment list during the date
        List<PayPalPayment> payPalPayments = payPalPaymentRepository.findByCreateTimeBetweenAndPaymentState(startDate, endDate, "complete");

        // generate report
        Map<LocalDate, DailyReport> reportMap = calculateDailyTotals(payPalPayments);
        log.info(reportMap);

        if ( type.equals("JSON")) {
            return generateJsonFile(reportMap);
        } else { // csv
            return generateCsvFile(reportMap);
        }
    }

    /**
     * reconcile every transaction
     * @param file
     * @return
     */
    @Override
    public List<ReconcileResult> readTransactions(MultipartFile file) {

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            CsvToBean<PaypalTransactionRecord> csvToBean = new CsvToBeanBuilder<PaypalTransactionRecord>(reader)
                    .withType(PaypalTransactionRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(1) // Skip the first 6 lines which might not be actual data
                    .build();

            List<PaypalTransactionRecord> paypalTransactionRecords= csvToBean.parse();

            List<ReconcileResult> reconcileResultList = reconcilePayPalRecordsAndDBRecords(paypalTransactionRecords);
            return reconcileResultList;
        } catch (Exception e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

    private List<ReconcileResult> reconcilePayPalRecordsAndDBRecords(List<PaypalTransactionRecord> paypalTransactionRecords) {
        List<ReconcileResult> reconcileResultList = new ArrayList<>();

        for (PaypalTransactionRecord transactionRecord : paypalTransactionRecords) {
            ReconcileResult reconcileResult = new ReconcileResult();
            reconcileResult.setPaypalTransactionRecord(transactionRecord);
            log.info("transactionRecord: " + transactionRecord);

            // get paypal payment db record
            String salesOrderSn = transactionRecord.getSalesOrderSn();
            PayPalPayment payPalPayment = payPalPaymentRepository.findBySalesOrderSn(salesOrderSn)
                    .orElse(null);

            try {
                if (payPalPayment == null) {
                    reconcileResult.setPaypalDBPaymentRecord(null);
                    log.info("transactionRecord does not match!");
                    throw new DBPaymentNotExitException("DB does not include the payment record", ErrorCategory.PAYMENT_NOT_EXIT_IN_DB, salesOrderSn);
                }
                log.info("paypal db records: " + payPalPayment);

                // reconcile db and transactionRecord
                if (!isReconciliationWithPayPalSuccessful(transactionRecord, payPalPayment)) {
                    reconcileResult.setPaypalDBPaymentRecord(payPalPayment);
                    throw new PaymentRecordNotMatchException("transaction and payment db Records does not match!", ErrorCategory.PAYMENT_RECORDS_NOT_MATCH, salesOrderSn, payPalPayment.getTransactionId());
                }
                reconcileResult.setPaypalDBPaymentRecord(payPalPayment);
                log.info("match!");
            } catch (DBPaymentNotExitException e) {
                ReconcileErrorLog errorLog = reconcileErrorLogService.logError(e, "ERROR: no such payment exit");
                reconcileResult.setReconcileErrorLog(errorLog);
            } catch (PaymentRecordNotMatchException e) {
                ReconcileErrorLog errorLog = reconcileErrorLogService.logError(e, "ERROR: Payment Records Do Not Match!");
                reconcileResult.setReconcileErrorLog(errorLog);
            } catch (NoSuchElementException e) {
                throw new RuntimeException("no such payment exit: " + e.getMessage());
            } finally {
                // This block will execute whether or not an exception occurred
                reconcileResultList.add(reconcileResult);
            }
        }
        return reconcileResultList;
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

    public Map<LocalDate, DailyReport> calculateDailyTotals(List<PayPalPayment> payPalPayments) {
        Map<LocalDate, DailyReport> reportMap = new HashMap<>();

        for ( PayPalPayment payment : payPalPayments ) {

            LocalDate date = payment.getUpdatedAt().toLocalDate();

            DailyReport dailyReport = reportMap.getOrDefault(date, new DailyReport());

            dailyReport.setPaymentsReceived(dailyReport.getPaymentsReceived() + 1);
            dailyReport.setFees(dailyReport.getFees().add(BigDecimal.valueOf(payment.getPayPalFee())));
            dailyReport.setNetAmount(dailyReport.getNetAmount().add(BigDecimal.valueOf(payment.getNet())));
            dailyReport.setTotalAmountReceived(dailyReport.getNetAmount().add(dailyReport.getFees()));

            reportMap.put(date, dailyReport);
        }

        return reportMap;
    }

    /**
     * This method checks the four conditions:
     * payPalPayment's paymentState is "COMPLETE".
     * salesOrder's orderStatus is "PROCESSING".
     * The sum of payPalPayment's payPalFee and net equals salesOrder's totalAmount.
     * payPalPayment's updatedAt is before salesOrder's expirationDate.
     */
    private boolean isReconciliationSuccessful(SalesOrder salesOrder, PayPalPayment payPalPayment) {
        boolean isPaymentStateComplete = "complete".equalsIgnoreCase(payPalPayment.getPaymentState());
        boolean isOrderStateProcessing = "PROCESSING".equalsIgnoreCase(salesOrder.getOrderStatus().name());
        Double totalPayPalAmount = payPalPayment.getPayPalFee() + payPalPayment.getNet();
        boolean isAmountMatching = totalPayPalAmount.equals(salesOrder.getTotalAmount().doubleValue());
        boolean isPaymentWithinExpiration = payPalPayment.getUpdatedAt().isBefore(salesOrder.getExpirationDate());
//        log.info(isOrderStateProcessing + " " + isOrderStateProcessing + " "  +  isAmountMatching + " " + isPaymentWithinExpiration);

        return isPaymentStateComplete && isOrderStateProcessing && isAmountMatching && isPaymentWithinExpiration;
    }

    private boolean isReconciliationWithPayPalSuccessful(PaypalTransactionRecord transactionRecord, PayPalPayment payPalPayment) {
        boolean isPaymentStateComplete = "complete".equalsIgnoreCase(payPalPayment.getPaymentState());
        boolean isPaymentFeeMatching = Double.parseDouble(transactionRecord.getFees()) == (-payPalPayment.getPayPalFee());
        boolean isPaymentNetMatching = Double.parseDouble(transactionRecord.getNet()) == payPalPayment.getNet();
        log.info(isPaymentStateComplete + " " + isPaymentFeeMatching + " "  +  isPaymentNetMatching);

        return isPaymentStateComplete && isPaymentFeeMatching && isPaymentNetMatching;
    }


}
