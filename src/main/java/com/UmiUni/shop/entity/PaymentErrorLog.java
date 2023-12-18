package com.UmiUni.shop.entity;

import com.UmiUni.shop.constant.ErrorCategory;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "PaymentErrorLog")
public class PaymentErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private String errorCode;

    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

//    private Long paymentId;

    private String transactionSn;

    private String salesOrderSn;

//    private Long customerId;

    private ErrorCategory errorType;

    private String description;

}
