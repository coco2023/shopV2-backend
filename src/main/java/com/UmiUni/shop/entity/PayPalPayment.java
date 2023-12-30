package com.UmiUni.shop.entity;

import com.UmiUni.shop.constant.OrderStatus;
import com.UmiUni.shop.constant.PaymentStatus;
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

    private String invoiceSn; // Assuming InvoiceSn is a unique identifier but not a foreign key

    private String salesOrderSn;

    @Column(name = "transactionId_id")
    private String transactionId;

    private String paymentState;  // reco

    private String paymentMethod;

    private String status;  // payment status // reco

    private Double payPalFee;  // PayPal service Fee // reco

    private Double net;  // reco

//    private Double totalAmount;

    private String payerId;

    private String merchantId;  // reco

    private String supplierId; // ims

    private LocalDateTime createTime;

    private LocalDateTime updatedAt; // reco

    private Double tax;  // reco

}
