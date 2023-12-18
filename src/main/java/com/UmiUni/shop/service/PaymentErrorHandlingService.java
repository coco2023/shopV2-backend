package com.UmiUni.shop.service;

import com.UmiUni.shop.constant.ErrorCategory;
import com.UmiUni.shop.constant.PaymentStatus;
import com.UmiUni.shop.entity.PaymentErrorLog;
import com.UmiUni.shop.exception.PaymentExpiredException;
import com.UmiUni.shop.exception.PaymentProcessingException;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.repository.PaymentErrorLogRepo;
import com.paypal.base.rest.PayPalRESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;


@Service
public class PaymentErrorHandlingService {

    @Autowired
    private PaymentErrorLogRepo paymentErrorLogRepo;

    private static final Logger log = LoggerFactory.getLogger(PaymentErrorHandlingService.class);

    public PaymentResponse handlePaymentExpiredError(PaymentExpiredException e ) {
        logError(e, "ERROR: Payment has been expired!");
        return new PaymentResponse(PaymentStatus.EXPIRED.name(), null, "payment expired", e.getMessage(), null);
    }

    public PaymentResponse handlePaymentProcessingError(PaymentProcessingException e) {
        logError(e, "Error during payment processing: user exist!");
        // Additional logic for PaymentProcessingException
        return new PaymentResponse(PaymentStatus.FAILED.name(), null, "Payment processing failed", e.getMessage(), null);
    }

    public PaymentResponse handlePayPalRESTError(PayPalRESTException e) {
        logError(e, "PayPal payment execution error");
        return new PaymentResponse(PaymentStatus.FAILED.name(), e.getMessage(), null, "PayPal payment failed", null);
    }

    public PaymentResponse handleGenericError(Exception e) {
        logError(e, "Unexpected error during payment");
        return new PaymentResponse("Unexpected error", e.getMessage(), null, "Unexpected error", null);
    }
    private void logError(Exception e, String message) {
        e.printStackTrace();
        log.error(message + ": " + e.getMessage(), e);

        ErrorCategory category = determineErrorCategory(e);
        // Log the error with its category
        log.error("Error Category: {}, {} : {}", category, message, e.getMessage(), e);
        saveErrorToDatabase(e, message, "SO-TEST-12345", category);

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
        return  ErrorCategory.CRITICAL;
    }

    private void rollbackTransactionIfNeeded() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    // save log to database
    private void saveErrorToDatabase(Exception e, String transactionSn, String salesOrderSn, ErrorCategory category) {
        PaymentErrorLog errorLog = PaymentErrorLog.builder()
                .errorCode(e.getClass().getSimpleName())
                .errorMessage(e.getMessage())
                .timestamp(LocalDateTime.now())
                .transactionSn(transactionSn)
                .salesOrderSn(salesOrderSn)
                .errorType(category)
                .build();
        paymentErrorLogRepo.save(errorLog);
    }

    // Methods for alerting, retry logic, user communication, etc., can be added here

}
