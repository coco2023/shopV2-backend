package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class PaymentRecordNotMatchException extends RuntimeException {
    private ErrorCategory category;

    private String salesOrderSn;

    private String transactionId;

    public PaymentRecordNotMatchException(String message) {
        super(message);
    }

    public PaymentRecordNotMatchException(String message, ErrorCategory category, String salesOrderSn, String transactionId) {
        super(message);
        this.category = category;
        this.salesOrderSn = salesOrderSn;
        this.transactionId = transactionId;
    }

    public ErrorCategory getCategory() {
        return category;
    }

    public String getSalesOrderSn() {
        return salesOrderSn;
    }

    public String getTransactionId() {
        return transactionId;
    }
}
