package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.ReconciliationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
public class ReconciliationServiceImpl implements ReconciliationService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

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
    public File generateMonthlySalesReport(LocalDateTime startDate, LocalDateTime endDate) {

        // get the payment list during the date
        List<PayPalPayment> payPalPayments = payPalPaymentRepository.findByCreateTimeBetweenAndPaymentState(startDate, endDate, "complete");

        // generate report
        Map<LocalDate, DailyReport> reportMap = calculateDailyTotals(payPalPayments);
        log.info(reportMap);

        return generateCsvFile(reportMap);
    }

    private File generateCsvFile(Map<LocalDate, DailyReport> reportMap) {
        // Create a temporary file to write the report to
        // Define the path to the resources directory
        String resourcesDir = Paths.get("doc", "report").toString();

        File csvOutputFile = new File(resourcesDir,"monthly_sales_report.csv");
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


}
