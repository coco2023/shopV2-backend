package com.UmiUni.shop.entity;

import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalResource;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "PayPal_Payments")
public class PayPalPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "paypal_token", unique = true, nullable = false)
    private String paypalToken;

    @Column(name = "transactionId_id")
    private String transactionId;

    private String paymentState;

    private String paymentMethod; // e.g., "Credit Card", "PayPal"

    private LocalDateTime createTime;

    private LocalDateTime updatedAt;

}
