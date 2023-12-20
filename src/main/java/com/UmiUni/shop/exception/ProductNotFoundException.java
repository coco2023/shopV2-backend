package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class ProductNotFoundException extends RuntimeException {

    private ErrorCategory category;

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, ErrorCategory category) {
        super(message);
        this.category = category;
    }

    public ErrorCategory getCategory() {
        return category;
    }

}
