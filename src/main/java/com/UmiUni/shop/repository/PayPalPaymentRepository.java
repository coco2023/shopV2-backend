package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.PayPalPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayPalPaymentRepository extends JpaRepository<PayPalPayment, Long> {

    // Method to find a payment by its PayPal token
    PayPalPayment findByPaypalToken(String paypalToken);

}
