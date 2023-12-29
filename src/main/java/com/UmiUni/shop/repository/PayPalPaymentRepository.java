package com.UmiUni.shop.repository;

import com.UmiUni.shop.entity.PayPalPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayPalPaymentRepository extends JpaRepository<PayPalPayment, Long> {

    // Method to find a payment by its PayPal token
    PayPalPayment findByPaypalToken(String paypalToken);

    Optional<PayPalPayment> findBySalesOrderSn(String salesOrderSn);

    List<PayPalPayment> getPayPalPaymentsByCreateTimeAfterAndPaymentState(LocalDateTime day, String status);

    List<PayPalPayment> findByCreateTimeBetweenAndPaymentState(LocalDateTime startDate, LocalDateTime endDate, String status);

    PayPalPayment findByTransactionId(String transactionId);

    // SupplierPaymentService
    PayPalPayment findBySupplierIdAndId(String supplierId, Long id);

    List<PayPalPayment> findAllBySupplierId(String supplierId);

    List<PayPalPayment> findBySupplierIdAndSalesOrderSn(String supplierId, String salesOrderSn);

}
