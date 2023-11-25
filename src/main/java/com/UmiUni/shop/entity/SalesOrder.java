package com.UmiUni.shop.entity;

import com.UmiUni.shop.constant.OrderStatus;
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
@Table(name = "SalesOrders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesOrderId;

    @Column(unique = true)
    private String salesOrderSn;

    @Column(nullable = false)
    private Long customerId;  // Assuming CustomerID is a unique identifier but not a foreign key

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String billingAddress;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(columnDefinition = "TEXT")
    private String paymentMethod;

    @Column(name = "payment_processed")
    private Boolean paymentProcessed;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}
