package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class AmqpException extends RuntimeException {

    private ErrorCategory category;

    public AmqpException(String message) {
        super(message);
    }

    public AmqpException(String message, ErrorCategory category) {
        super(message);
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }

}
