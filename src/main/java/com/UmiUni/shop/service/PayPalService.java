package com.UmiUni.shop.service;

import com.UmiUni.shop.dto.PayPalPaymentResponseDTO;
import com.UmiUni.shop.entity.SalesOrder;
import com.UmiUni.shop.model.PaymentResponse;

import java.util.List;

public interface PayPalService {
    PaymentResponse createPayment(SalesOrder salesOrder);

    PaymentResponse completePayment(String paymentId, String payerId);

//    String getPaymentStatus(String token);

    PaymentResponse checkPaymentStatus(String token) throws Exception;

    List<PayPalPaymentResponseDTO> getAllPayPalPaymentResponseEntity();
}
