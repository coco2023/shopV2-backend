package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class PaymentExpiredException extends RuntimeException {

    private ErrorCategory category;

    public PaymentExpiredException(String message) {
        super(message);
    }

    public PaymentExpiredException(String message, ErrorCategory category) {
        super(message);
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }
}
