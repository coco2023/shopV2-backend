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
@Table(name = "ReconcileErrorLog")
public class ReconcileErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String errorCode;

    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    private String transactionSn;

    private String salesOrderSn;

    private ErrorCategory errorType;

    private String description;

    private LocalDateTime timestamp;

}
