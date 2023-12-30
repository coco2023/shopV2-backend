package com.UmiUni.shop.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Entity
@Table(name = "SupplierFinance")
public class SupplierFinance {

    /**
     * whole count
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate reportDate; // Reporting date
    private Long supplierId;

    // transaction details
    private int paymentsNumReceived; // Number of payments received in a day/month/year // 到账总数

    private Double totalAmountReceived; // Total transaction amount  // 到账总额
    private Double totalTax; // Total tax amount            // 税费
    private Double totalServiceFee; // Total service fees        // 服务费
    private Double otherFees; // Total fees for the day/month/year   // 其他服务费
    private Double totalNetAmount; // Net amount for the day/month/year  // 净利润

    private Double accountsReceivable; // 应收账款 (Accounts Receivable)
    private Double actualReceipts; // 实收账款 (Actual Receipts)
    private Double outstandingAccounts; // 未结清账款 (Outstanding Accounts)

    // Overall financial status
//    private LocalDateTime startDate; // Start date of the reporting period
    private Double openingBalance; // Opening balance at the start of the period // 期初余额
//    private LocalDateTime endDate; // End date of the reporting period
    private Double closingBalance; // Closing balance at the end of the period // 期末余额

    // Additional financial details
//    private LocalDateTime fundsReceivedDate; // Date when funds are received // 到账日期

}
