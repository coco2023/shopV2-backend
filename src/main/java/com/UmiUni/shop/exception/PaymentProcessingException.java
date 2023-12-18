package com.UmiUni.shop.exception;

import com.UmiUni.shop.constant.ErrorCategory;

public class PaymentProcessingException extends RuntimeException {

    private ErrorCategory category;

    public PaymentProcessingException(String message) {
        super(message);
    }

    // Constructor
    public PaymentProcessingException(String message, ErrorCategory category) {
        super(message);
        this.category = category;
    }

    // Getter
    public ErrorCategory getCategory() {
        return category;
    }

}
//public class PaymentProcessingException extends Exception {
//    private String additionalDetails;
//
//    public PaymentProcessingException(String message, String additionalDetails) {
//        super(message);
//        this.additionalDetails = additionalDetails;
//    }
//
//    // Other constructors as needed
//
//    public String getAdditionalDetails() {
//        return additionalDetails;
//    }
//
//    // Other methods
//}
