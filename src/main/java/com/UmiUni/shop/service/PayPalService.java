package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PayPalPaymentResponse;
import com.UmiUni.shop.model.PaymentResponse;

public interface PayPalService {
    PayPalPaymentResponse createPayment(SalesOrder salesOrder);
    PaymentResponse completePayment(String paymentId, String payerId);
}
