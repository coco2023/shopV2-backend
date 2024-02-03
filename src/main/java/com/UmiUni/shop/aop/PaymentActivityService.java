package com.UmiUni.shop.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentActivityService {

    @Autowired
    private PaymentActivityRepository paymentActivityRepository;

    public PaymentActivity recordPaymentActivity(String salesOrderSn, String activityType, String status, String transactionId, String approvalUrl, String errorMessage, String description) {
        PaymentActivity paymentActivity = new PaymentActivity();
        paymentActivity.setSalesOrderSn(salesOrderSn);
        paymentActivity.setActivityType(activityType);
        paymentActivity.setStatus(status);
        paymentActivity.setTransactionId(transactionId);
        paymentActivity.setApprovalUrl(approvalUrl);
        paymentActivity.setErrorMessage(errorMessage);
        paymentActivity.setDescription(description);
        paymentActivity.setCreatedAt(LocalDateTime.now());
        return paymentActivityRepository.save(paymentActivity);
    }
}
