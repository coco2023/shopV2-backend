package com.UmiUni.shop.entity;

import com.UmiUni.shop.constant.PaymentStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "Payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = true)
    private Long invoiceId; // Assuming InvoiceID is a unique identifier but not a foreign key

    private String transactionId; // Transaction ID from the payment gateway

//    private Long salesOrderId;    #TODO: can not get salesOrderId yet
    private String salesOrderSn;

    private LocalDateTime paymentDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "customer_email")
    private String customerEmail;

    private String paymentMethod; // e.g., "Credit Card", "PayPal"

    @Column(name = "payment_method_details")
    private String paymentMethodDetails;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "receipt_url")
    private String receiptUrl; // URL to the receipt, if available

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
