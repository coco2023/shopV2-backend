package com.UmiUni.shop.service;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.entity.ReconcileErrorLog;
import com.UmiUni.shop.exception.DBPaymentNotExitException;
import com.UmiUni.shop.exception.PaymentRecordNotMatchException;
import com.UmiUni.shop.repository.PayPalPaymentRepository;
import com.UmiUni.shop.repository.ReconcileErrorLogRepo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Service
@Log4j2
public class ReconcileErrorLogService {
    @Autowired
    private ReconcileErrorLogRepo reconcileErrorLogRepo;

    @Autowired
    private PayPalPaymentRepository payPalPaymentRepository;

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
        int maxLines = 10; // Number of lines to include in the summary
        String[] lines = stackTrace.split("\n");
        StringBuilder summary = new StringBuilder();

        for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
            summary.append(lines[i]).append("\n");
        }
        return summary.toString();
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
            return ((DBPaymentNotExitException) e).getSalesOrderSn();
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

}
