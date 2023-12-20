package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class InsufficientStockException extends RuntimeException {

    private ErrorCategory category;

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, ErrorCategory category) {
        super(message);
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }

}
