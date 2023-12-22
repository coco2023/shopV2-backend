package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.PaymentErrorLog;
import com.UmiUni.shop.entity.ReconcileErrorLog;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.exception.DBPaymentNotExitException;
import com.UmiUni.shop.exception.PaymentRecordNotMatchException;
import com.UmiUni.shop.model.DailyReport;
import com.UmiUni.shop.model.PaypalTransactionRecord;
import com.UmiUni.shop.model.ReconcileOrderAndPayment;
import com.UmiUni.shop.model.ReconcileResult;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.SalesOrderRepository;
import com.UmiUni.shop.service.ReconcileErrorLogService;
import com.UmiUni.shop.service.ReconciliationService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public ReconcileOrderAndPayment reconcilePaymentViaSalesOrderSn(String salesOrderSn) {
        Optional<SalesOrder> salesOrderOpt = salesOrderRepository.getSalesOrderBySalesOrderSn(salesOrderSn);
        Optional<PayPalPayment> payPalPaymentOpt = payPalPaymentRepository.findBySalesOrderSn(salesOrderSn);

        try {
            if (salesOrderOpt.isPresent() && payPalPaymentOpt.isPresent()) {
                SalesOrder salesOrder = salesOrderOpt.get();
                PayPalPayment payPalPayment = payPalPaymentOpt.get();
                log.info("salesOrder: " + salesOrder.getOrderStatus() + " payPalPayment: " + payPalPayment.getPaymentState());

                if (isReconciliationSuccessful(salesOrder, payPalPayment)) {
                    ReconcileOrderAndPayment reconcileResult = ReconcileOrderAndPayment.builder()
                            .payPalPayment(payPalPayment)
                            .salesOrder(salesOrder)
                            .reconcileErrorLog(null)
                            .build();
                    return reconcileResult; //"Reconciliation successful";
                } else {
                    throw new RuntimeException("Reconciliation failed!");
                }
            } else {
                throw new RuntimeException("SalesOrder or PayPalPayment not found!");
            }
        }catch (Exception e) {
            ReconcileOrderAndPayment reconcileResult = ReconcileOrderAndPayment.builder()
                    .payPalPayment(null)
                    .salesOrder(null)
                    .reconcileErrorLog(e.getMessage())
                    .build();
            log.error(e.getMessage() + " " + reconcileResult);
            return reconcileResult;
        }
    }

    @Override
    public List<ReconcileOrderAndPayment> reconcilePastDays(int days) {

        List<ReconcileOrderAndPayment> reconcileResult = new ArrayList<>();

        LocalDateTime daysBefore = LocalDateTime.now().minusDays(days);
        List<SalesOrder> recentSalesOrders = salesOrderRepository.getSalesOrdersByOrderDateAfterAndOrderStatus(daysBefore, OrderStatus.PROCESSING);
//        List<PayPalPayment> recentPayments = payPalPaymentRepository.getPayPalPaymentsByCreateTimeAfterAndPaymentState(daysBefore, "complete");

        for (SalesOrder order : recentSalesOrders) {
            ReconcileOrderAndPayment reconcile = processSingleOrder(order);
            reconcileResult.add(reconcile);
        }

        return reconcileResult;
    }

    private ReconcileOrderAndPayment processSingleOrder(SalesOrder order) {
        try {
            PayPalPayment payment = payPalPaymentRepository.findBySalesOrderSn(order.getSalesOrderSn())
                    .orElseThrow(() -> new PaymentRecordNotMatchException(
                            "PayPal Payment is None! SalesOrderSn: " + order.getSalesOrderSn(),
                            ErrorCategory.RECONCILE_PAYMENT_NOT_EXIT_IN_DB,
                            order.getSalesOrderSn(),
                            null
                    ));
            if (!isReconciliationSuccessful(order, payment)) {
                throw new PaymentRecordNotMatchException("SalesOrder and PayPal Payment do not match! payment transactionId : " + payment.getTransactionId() + "; SalesOrderSn: " + order.getSalesOrderSn(),
                        ErrorCategory.RECONCILE_PAYMENT_RECORDS_NOT_MATCH,
                        order.getSalesOrderSn(),
                        null);
            }
            return createReconciliationEntry(order, payment, null);
        } catch (PaymentRecordNotMatchException e) {
            SalesOrder salesOrder = salesOrderRepository.getSalesOrderBySalesOrderSn(e.getSalesOrderSn()).get();
            return createReconciliationEntry(salesOrder, null, e.getMessage());
        } catch (Exception e) {
            return createReconciliationEntry(null, null, e.getMessage());
        }
    }

    private ReconcileOrderAndPayment createReconciliationEntry(SalesOrder salesOrder, PayPalPayment payPalPayment, String errorMessage) {
        return ReconcileOrderAndPayment.builder()
                .salesOrder(salesOrder)
                .payPalPayment(payPalPayment)
                .reconcileErrorLog(errorMessage)
                .build();
    }

    @Override
    public List<ReconcileOrderAndPayment> reconcileBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<ReconcileOrderAndPayment> reconcileResult = new ArrayList<>();

        //get all the order & payment records between these days with valid status
        List<SalesOrder> salesOrders = salesOrderRepository.findByOrderDateBetweenAndOrderStatus(startDate, endDate, OrderStatus.PROCESSING);

        for (SalesOrder order : salesOrders) {
            ReconcileOrderAndPayment reconcile = processSingleOrder(order);
            reconcileResult.add(reconcile);
        }

        return reconcileResult;
    }

    @Override
    public Map<LocalDate, DailyReport> generateMonthlySalesReport(LocalDateTime startDate, LocalDateTime endDate, String type) {

        // get the payment list during the date
        List<PayPalPayment> payPalPayments = payPalPaymentRepository.findByCreateTimeBetweenAndPaymentState(startDate, endDate, "complete");

        // generate report
        Map<LocalDate, DailyReport> reportMap = new HashMap<>();
        for ( PayPalPayment payment : payPalPayments ) {
            LocalDate date = payment.getUpdatedAt().toLocalDate();
            DailyReport dailyReport = calculateDailyTotals(
                    reportMap.getOrDefault(date, new DailyReport()),
                    payment);
            reportMap.put(date, dailyReport);
        }

        log.info(reportMap);
        return reportMap;
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
                    throw new DBPaymentNotExitException("DB does not include the payment record", ErrorCategory.RECONCILE_PAYMENT_NOT_EXIT_IN_DB, salesOrderSn);
                }
                log.info("paypal db records: " + payPalPayment);

                // reconcile db and transactionRecord
                if (!isReconciliationWithPayPalSuccessful(transactionRecord, payPalPayment)) {
                    reconcileResult.setPaypalDBPaymentRecord(payPalPayment);
                    throw new PaymentRecordNotMatchException("transaction and payment db Records does not match!", ErrorCategory.RECONCILE_PAYMENT_RECORDS_NOT_MATCH, salesOrderSn, payPalPayment.getTransactionId());
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
                ReconcileErrorLog errorLog = reconcileErrorLogService.logError(e, "no such payment exit: " + e.getMessage());
                reconcileResult.setReconcileErrorLog(errorLog);
            } finally {
                reconcileResultList.add(reconcileResult);
            }
        }
        return reconcileResultList;
    }

    public DailyReport calculateDailyTotals(DailyReport dailyReport, PayPalPayment payment) {
        dailyReport.setPaymentsReceived(dailyReport.getPaymentsReceived() + 1);
        dailyReport.setFees(dailyReport.getFees().add(BigDecimal.valueOf(payment.getPayPalFee())));
        dailyReport.setNetAmount(dailyReport.getNetAmount().add(BigDecimal.valueOf(payment.getNet())));
        dailyReport.setTotalAmountReceived(dailyReport.getNetAmount().add(dailyReport.getFees()));
        return dailyReport;
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
        boolean isSalesOrderSnMatch = salesOrder.getSalesOrderSn().equals(payPalPayment.getSalesOrderSn());
        boolean isOrderStateProcessing = "PROCESSING".equalsIgnoreCase(salesOrder.getOrderStatus().name());
        Double totalPayPalAmount = payPalPayment.getPayPalFee() + payPalPayment.getNet();
        boolean isAmountMatching = totalPayPalAmount.equals(salesOrder.getTotalAmount().doubleValue());
        boolean isPaymentWithinExpiration = payPalPayment.getUpdatedAt().isBefore(salesOrder.getExpirationDate());
//        log.info(isOrderStateProcessing + " " + isOrderStateProcessing + " "  +  isAmountMatching + " " + isPaymentWithinExpiration);

        return isPaymentStateComplete && isOrderStateProcessing && isAmountMatching && isPaymentWithinExpiration && isSalesOrderSnMatch;
    }

    private boolean isReconciliationWithPayPalSuccessful(PaypalTransactionRecord transactionRecord, PayPalPayment payPalPayment) {
        boolean isPaymentStateComplete = "complete".equalsIgnoreCase(payPalPayment.getPaymentState());
        boolean isPaymentFeeMatching = Double.parseDouble(transactionRecord.getFees()) == (-payPalPayment.getPayPalFee());
        boolean isPaymentNetMatching = Double.parseDouble(transactionRecord.getNet()) == payPalPayment.getNet();
        log.info(isPaymentStateComplete + " " + isPaymentFeeMatching + " "  +  isPaymentNetMatching);

        return isPaymentStateComplete && isPaymentFeeMatching && isPaymentNetMatching;
    }


}
