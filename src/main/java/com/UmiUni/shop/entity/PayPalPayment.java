package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

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

    private Date createTime;

    private String paymentState;

}
