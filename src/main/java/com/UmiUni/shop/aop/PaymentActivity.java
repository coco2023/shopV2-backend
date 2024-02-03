package com.UmiUni.shop.aop;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "payment_activity")
public class PaymentActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_order_sn")
    private String salesOrderSn;

    @Column(name = "activity_type")
    private String activityType;

    @Column(name = "status")
    private String status;

    @Column(name = "transaction_id")
    private String transactionId;

    private String approvalUrl;

    private String description;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
