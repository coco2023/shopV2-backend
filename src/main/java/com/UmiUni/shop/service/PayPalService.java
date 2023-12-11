package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;
import com.UmiUni.shop.model.PaymentStatusResponse;
import com.paypal.base.rest.PayPalRESTException;

public interface PayPalService {
    PayPalPaymentResponse createPayment(SalesOrder salesOrder);
    PaymentResponse completePayment(String paymentId, String payerId);

//    String getPaymentStatus(String token);

    PaymentStatusResponse checkPaymentStatus(String token) throws Exception;
}
