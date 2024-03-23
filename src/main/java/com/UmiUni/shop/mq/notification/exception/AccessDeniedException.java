package com.UmiUni.shop.mq.notification.exception;

import org.springframework.security.core.AuthenticationException;

public class AccessDeniedException extends AuthenticationException {

    // Constructor with a message
    public AccessDeniedException(String msg) {
        super(msg);
    }

    // Constructor with a message and a root cause Throwable
    public AccessDeniedException(String msg, Throwable t) {
        super(msg, t);
    }
}
