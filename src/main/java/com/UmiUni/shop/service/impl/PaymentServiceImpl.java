package com.UmiUni.shop.service.impl;

import com.UmiUni.shop.entity.Payment;
import com.UmiUni.shop.entity.PaymentErrorLog;
import com.UmiUni.shop.repository.PaymentRepository;
import com.UmiUni.shop.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = getPayment(id);
        payment.setInvoiceSn(paymentDetails.getInvoiceSn());
        payment.setTransactionId(paymentDetails.getTransactionId());
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentStatus(paymentDetails.getPaymentStatus());
        payment.setPaymentMethod(paymentDetails.getPaymentMethod());
        // other updates as needed
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
