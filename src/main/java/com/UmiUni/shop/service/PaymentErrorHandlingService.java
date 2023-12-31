package com.UmiUni.shop.service;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.entity.PaymentErrorLog;
import com.UmiUni.shop.exception.*;
import com.UmiUni.shop.model.InventoryUpdateMessage;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.repository.PaymentErrorLogRepo;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static com.UmiUni.shop.utils.ExtractSummary.extractSummaryToGetString;


@Service
public class PaymentErrorHandlingService {

    @Autowired
    private PaymentErrorLogRepo paymentErrorLogRepo;

    private static final Logger log = LoggerFactory.getLogger(PaymentErrorHandlingService.class);

    public PaymentResponse handlePaymentExpiredError(PaymentExpiredException e,
                                                     String transactionId,
                                                     String salesOrderId
    ) {
        logError(e, "ERROR: Payment has been expired!", transactionId, salesOrderId);
        return new PaymentResponse(PaymentStatus.EXPIRED.name(), null, "payment expired", e.getMessage(), null);
    }

    public PaymentResponse handlePaymentProcessingError(PaymentProcessingException e,
                                                        String transactionId,
                                                        String salesOrderId
    ) {
        logError(e, "Error during payment processing: user exist!", transactionId, salesOrderId);
        // Additional logic for PaymentProcessingException
        return new PaymentResponse(PaymentStatus.FAILED.name(), null, "Payment processing failed", e.getMessage(), null);
    }

    public PaymentResponse handlePayPalRESTError(PayPalRESTException e,
                                                 String transactionId,
                                                 String salesOrderId
    ) {
        logError(e, "PayPal payment execution error", transactionId, salesOrderId);
        return new PaymentResponse(PaymentStatus.FAILED.name(), e.getMessage(), null, "PayPal payment failed", null);
    }

    public PaymentResponse handleGenericError(Exception e,
                                              String transactionId,
                                              String salesOrderId
    ) {
        logError(e, "Unexpected error during payment", transactionId, salesOrderId);
        return new PaymentResponse("Unexpected error", e.getMessage(), null, "Unexpected error", null);
    }

    private void logError(Exception e, String message,
                          String transactionId,
                          String salesOrderId
    ) {
        e.printStackTrace();
        log.error(message + ": " + e.getMessage(), e);

        ErrorCategory category = determineErrorCategory(e);
        // Log the error with its category
        log.error("Error Category: {}, {} : {}", category, message, e.getMessage(), e);
        saveErrorToDatabase(e, message, transactionId, salesOrderId, category);

//        handleCategorySpecificActions(category, e);
        rollbackTransactionIfNeeded();
    }

    private void handleCategorySpecificActions(ErrorCategory category, Exception e) {
        switch (category) {
            case TRANSIENT:
                // Logic for transient errors
                break;
            case CLIENT_EXIT:
                break;
            case CLIENT_ERROR:
                // Logic for client errors
                break;
            case SERVER_ERROR:
                // Logic for server errors
                break;
            case CRITICAL:
                // Logic for critical errors
                break;
        }
    }

    private ErrorCategory determineErrorCategory(Exception e) {
        // Handle PaymentProcessingException
        if (e instanceof PaymentProcessingException) {
            PaymentProcessingException pe = (PaymentProcessingException) e;
            // Log additional details specific to PaymentProcessingException
            log.error("PaymentProcessingException details: " + pe.getMessage());
            // Add any additional logging or handling here
            return ((PaymentProcessingException) e).getCategory();
        }

        // Handle PayPalRESTException
        if (e instanceof PayPalRESTException) {
            PayPalRESTException pre = (PayPalRESTException) e;
            // Log additional details specific to PayPalRESTException
            log.error("PayPalRESTException details: " + pre.getDetails());
            // Add any additional logging or handling here
            return ErrorCategory.SERVER_ERROR;
        }

        if (e instanceof ProductNotFoundException) {
            ProductNotFoundException pre = (ProductNotFoundException) e;
            log.error("ProductNotFoundException: " + pre.getMessage());
            return ErrorCategory.PRODUCT_NOT_FOUND;
        }

        if (e instanceof DBPaymentNotExitException) {
            DBPaymentNotExitException de = (DBPaymentNotExitException) e;
            log.error("DBPaymentNotExitException: " + de.getMessage());
            return ErrorCategory.RECONCILE_PAYMENT_NOT_EXIT_IN_DB;
        }

        if (e instanceof PaymentRecordNotMatchException) {
            PaymentRecordNotMatchException nme = (PaymentRecordNotMatchException) e;
            log.error("PaymentRecordNotMatchException details: " + nme.getMessage());
            return ErrorCategory.RECONCILE_PAYMENT_RECORDS_NOT_MATCH;
        }

        if (e instanceof PaymentExpiredException) {
            PaymentExpiredException pe = (PaymentExpiredException) e;
            log.error("PaymentExpiredException: " + pe.getMessage());
            return ErrorCategory.ORDER_EXPIRED;
        }

        if (e instanceof InsufficientStockException) {
            InsufficientStockException ie = (InsufficientStockException) e;
            log.error("InsufficientStockException: " + ie.getMessage());
            return ErrorCategory.INSUFFICIENT_STOCK;
        }

        if (e instanceof AmqpException) {
            AmqpException ae = (AmqpException) e;
            log.error("AmqpException: " + ae.getMessage());
            return ErrorCategory.AMQP_ERROR;
        }

        return  ErrorCategory.CRITICAL;
    }

    private void rollbackTransactionIfNeeded() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    // save log to database
    private void saveErrorToDatabase(Exception e, String description, String transactionSn, String salesOrderSn, ErrorCategory category) {

        String stackTrace = getStackTraceAsString(e);

        PaymentErrorLog errorLog = PaymentErrorLog.builder()
                .errorCode(e.getClass().getSimpleName())
                .errorMessage(e.getMessage())
                .timestamp(LocalDateTime.now())
                .transactionSn(transactionSn)
                .salesOrderSn(salesOrderSn)
                .errorType(category)
                .stackTrace(stackTrace)
                .description(description)
                .build();
        paymentErrorLogRepo.save(errorLog);
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String fullStackTrace = stringWriter.toString();

//        // method 1: limit the text size
//        return truncateStack(fullStackTrace);

        // method 2: extract the error
        return extractSummary(fullStackTrace);
    }

    private String truncateStack(String fullStackTrace) {
        // Truncate the stack trace if it's too long
        int maxStackTraceLength = 3000;
        return fullStackTrace.length() > maxStackTraceLength ?
                fullStackTrace.substring(0, maxStackTraceLength) :
                fullStackTrace;
    }

    private String extractSummary(String stackTrace) {
        // Extract the first few lines or key parts of the stack trace
        return extractSummaryToGetString(stackTrace);
    }

    public void handleProductNotFoundException(Exception e, String skuCode, String message) {
        logProductError(e, skuCode, message);
    }

    public void handleInsufficientStockException(Exception e, String skuCode, String message) {
        logProductError(e, skuCode, message);
    }

    private void logProductError(Exception e, String skuCode, String message) {
        e.printStackTrace();
        log.error(message + ": " + e.getMessage(), e);

        ErrorCategory category = determineErrorCategory(e);
        // Log the error with its category
        log.error("Error Category: {}, {} : {}", category, message, e.getMessage(), e);
        saveProductErrorToDatabase(e, message, skuCode, category);
        rollbackTransactionIfNeeded();
    }

    private void saveProductErrorToDatabase(Exception e, String message, String skuCode, ErrorCategory category) {
        String stackTrace = getStackTraceAsString(e);

        PaymentErrorLog errorLog = PaymentErrorLog.builder()
                .errorCode(e.getClass().getSimpleName())
                .errorMessage(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errorType(category)
                .stackTrace(stackTrace)
                .description("skuCode: " + skuCode + "," + message)
                .build();
        paymentErrorLogRepo.save(errorLog);
    }

    public void handleAmqpException(AmqpException e, InventoryUpdateMessage message) {
        e.printStackTrace();
        log.error(message + ": " + e.getMessage(), e);

        ErrorCategory category = determineErrorCategory(e);
        // Log the error with its category
        log.error("Error Category: {}, {} : {}", category, message, e.getMessage(), e);

        // save error to db
        String stackTrace = getStackTraceAsString(e);

        PaymentErrorLog errorLog = PaymentErrorLog.builder()
                .errorCode(e.getClass().getSimpleName())
                .errorMessage(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errorType(category)
                .stackTrace(stackTrace)
                .description("AMQP sender error" + message)
                .build();
        paymentErrorLogRepo.save(errorLog);

    }

    // Methods for alerting, retry logic, user communication, etc., can be added here

}
