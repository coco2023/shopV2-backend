package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class DBPaymentNotExitException extends RuntimeException {
    private ErrorCategory category;

    private String salesOrderSn;

    public DBPaymentNotExitException(String message) {
        super(message);
    }

    public DBPaymentNotExitException(String message, ErrorCategory category, String orderSn) {
        super(message);
        this.category = category;
        this.salesOrderSn = orderSn;
    }

    public ErrorCategory getCategory() {
        return category;
    }
    public String getSalesOrderSn() {return salesOrderSn;}
}
