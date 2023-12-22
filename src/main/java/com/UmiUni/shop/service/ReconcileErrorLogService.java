package com.UmiUni.shop.service;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.entity.PaymentErrorLog;
import com.UmiUni.shop.entity.ReconcileErrorLog;
import com.UmiUni.shop.exception.DBPaymentNotExitException;
import com.UmiUni.shop.exception.PaymentRecordNotMatchException;
import com.UmiUni.shop.repository.PaymentErrorLogRepo;
import com.UmiUni.shop.repository.ReconcileErrorLogRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static com.UmiUni.shop.utils.ExtractSummary.extractSummaryToGetString;

@Service
@Log4j2
public class ReconcileErrorLogService {
    @Autowired
    private ReconcileErrorLogRepo reconcileErrorLogRepo;

    @Autowired
    private PaymentErrorLogRepo paymentErrorLogRepo;

    public ReconcileErrorLog logError(Exception e, String message) {
        e.printStackTrace();
        log.error(message + ": " + e.getMessage(), e);

        ErrorCategory category = determineErrorCategory(e);
        log.error("Error Category: {}, {} : {}", category, message, e.getMessage(), e);

        String salesOrderSn = determineSalesOrderId(e);

        String transactionId = determineTransactionId(e);

        ReconcileErrorLog reconcileErrorLog = saveErrorToDataBase(e, message, category, salesOrderSn, transactionId);

        return reconcileErrorLog;
    }

    private ReconcileErrorLog saveErrorToDataBase(Exception e, String message, ErrorCategory category, String salesOrderId, String transactionId) {
        String stackTrace = getStackTraceAsString(e);

//        // check if the paypal payment with the salesOrderSn is Empty
//        boolean isEmpty = payPalPaymentRepository.findBySalesOrderSn(salesOrderId).isEmpty();
//        log.info(payPalPaymentRepository.findBySalesOrderSn(salesOrderId) + "isEmpty: " + isEmpty);

        ReconcileErrorLog errorLog = ReconcileErrorLog.builder()
                .errorCode(e.getClass().getSimpleName())
                .errorMessage(e.getMessage())
                .errorType(category)
                .transactionSn(transactionId)
                .salesOrderSn(salesOrderId)
                .stackTrace(stackTrace)
                .description(message)
                .timestamp(LocalDateTime.now())
                .build();
        reconcileErrorLogRepo.save(errorLog);
        return errorLog;
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String fullStackTrace = stringWriter.toString();
        return extractSummary(fullStackTrace);
    }

    private String extractSummary(String stackTrace) {
        return extractSummaryToGetString(stackTrace);
    }

    private String determineTransactionId(Exception e) {
        if (e instanceof PaymentRecordNotMatchException) {
            PaymentRecordNotMatchException nme = (PaymentRecordNotMatchException) e;
            log.error("PaymentRecordNotMatchException details: " + nme.getMessage());
            return nme.getTransactionId();
        }
        return null;
    }

    private String determineSalesOrderId(Exception e) {
        if (e instanceof DBPaymentNotExitException) {
            DBPaymentNotExitException de = (DBPaymentNotExitException) e;
            return de.getSalesOrderSn();
        }

        if (e instanceof PaymentRecordNotMatchException) {
            PaymentRecordNotMatchException nme = (PaymentRecordNotMatchException) e;
            log.error("PaymentRecordNotMatchException details: " + nme.getMessage());
            return nme.getSalesOrderSn();
        }
        return null;
    }

    private ErrorCategory determineErrorCategory(Exception e) {
        if (e instanceof DBPaymentNotExitException) {
            DBPaymentNotExitException de = (DBPaymentNotExitException) e;
            log.error("db does not have this payment record" + de.getMessage());
            return ((DBPaymentNotExitException) e).getCategory();
        }

        if (e instanceof PaymentRecordNotMatchException) {
            PaymentRecordNotMatchException nme = (PaymentRecordNotMatchException) e;
            log.error("PaymentRecordNotMatchException details: " + nme.getMessage());
            return nme.getCategory();
        }
        return ErrorCategory.OTHER_RECONCILE_ERROR;
    }

    /**
     * this code let to save the Reconcile result into PaymentErrorLog's database table
     * @param e
     * @param message
     * @return
     */
    public PaymentErrorLog logError2PaymentErrorLogDB(Exception e, String message) {
        e.printStackTrace();
        log.error(message + ": " + e.getMessage(), e);

        ErrorCategory category = determineErrorCategory(e);
        log.error("Error Category: {}, {} : {}", category, message, e.getMessage(), e);

        String salesOrderSn = determineSalesOrderId(e);

        String transactionId = determineTransactionId(e);

        PaymentErrorLog reconcileErrorLog = saveErrorToDataBase2(e, message, category, salesOrderSn, transactionId);

        return reconcileErrorLog;
    }

    private PaymentErrorLog saveErrorToDataBase2(Exception e, String message, ErrorCategory category, String salesOrderId, String transactionId) {
        String stackTrace = getStackTraceAsString(e);

//        // check if the paypal payment with the salesOrderSn is Empty
//        boolean isEmpty = payPalPaymentRepository.findBySalesOrderSn(salesOrderId).isEmpty();
//        log.info(payPalPaymentRepository.findBySalesOrderSn(salesOrderId) + "isEmpty: " + isEmpty);

        PaymentErrorLog errorLog = PaymentErrorLog.builder()
                .errorCode(e.getClass().getSimpleName())
                .errorMessage(e.getMessage())
                .errorType(category)
                .transactionSn(transactionId)
                .salesOrderSn(salesOrderId)
                .stackTrace(stackTrace)
                .description(message)
                .timestamp(LocalDateTime.now())
                .build();
        paymentErrorLogRepo.save(errorLog);
        return errorLog;
    }

}
