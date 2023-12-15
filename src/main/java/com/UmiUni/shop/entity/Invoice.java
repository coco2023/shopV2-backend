package com.UmiUni.shop.entity;

import com.UmiUni.shop.constant.PaidStatus;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "Invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    private String invoiceSn;

    @Column(nullable = false)
    private String salesOrderSn;  // Assuming SalesOrderSn is a unique identifier but not a foreign key

    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal itemsTotal;

    private BigDecimal itemsDiscount;

    @Column(nullable = false)
    private BigDecimal subTotal;

    private BigDecimal taxAmount;

    private BigDecimal shippingAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaidStatus paidStatus;
}
