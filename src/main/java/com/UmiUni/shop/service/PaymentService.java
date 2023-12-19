package com.UmiUni.shop.service;

import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.entity.PaymentErrorLog;

import java.util.List;

public interface PaymentService {
    Payment createPayment(Payment payment);
    Payment getPayment(Long id);
    List<Payment> getAllPayments();
    Payment updatePayment(Long id, Payment paymentDetails);
    void deletePayment(Long id);
}
