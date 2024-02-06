package com.UmiUni.shop.aop;

import com.UmiUni.shop.model.PaymentResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaymentServiceAspect {

    @Autowired
    private PaymentActivityService paymentActivityService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* com.UmiUni.shop.service.PayPalService.createPayment(..))")
    public void beforeCreatePayment(JoinPoint joinPoint) {
        logger.info("Before createPayment: {}", joinPoint.getSignature());
    }

    @AfterReturning(value = "execution(* com.UmiUni.shop.service.PayPalService.createPayment(..))", returning = "result")
    public void afterReturningCreatePayment(JoinPoint joinPoint, Object result) {
        logger.info("After createPayment: {}, Result: {}", joinPoint.getSignature(), result);
        PaymentResponse response = (PaymentResponse) result;
        paymentActivityService.recordPaymentActivity(null, "CREATED", response.getStatus(), response.getTransactionId(), response.getApprovalUrl(), response.getDescription(), response.getErrorMesg());
    }

    @AfterThrowing(value = "execution(* com.UmiUni.shop.service.PayPalService.createPayment(..))", throwing = "exception")
    public void afterThrowingCreatePayment(JoinPoint joinPoint, Throwable exception) {
        logger.error("Exception in createPayment: {}, Exception: {}", joinPoint.getSignature(), exception.getMessage());
    }

    // 类似地，可以为completePayment方法添加相应的切入点和通知
}
