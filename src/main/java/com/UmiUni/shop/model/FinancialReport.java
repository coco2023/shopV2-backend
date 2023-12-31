package com.UmiUni.shop.model;

import com.UmiUni.shop.constant.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
@ToString
public class FinancialReport {

    private Long id;
    private String reportDate; // Reporting date, daily
    private ReportType reportType; // DAILY, MONTHLY, YEARLY
    private Long supplierId;

    // transaction details
    private int paymentsNumReceived; // Number of payments received in a day/month/year // 到账总数
    private Double totalTax; // Total tax amount            // 税费
    private Double totalServiceFee; // Total service fees        // 服务费
    private Double otherFees; // Total fees for the day/month/year   // 其他服务费
    private Double totalNetAmount; // Net amount for the day/month/year  // 净利润
    private Double totalAmountReceived; // Total transaction amount  // 到账总额

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


    // Constructor
    public FinancialReport() {
        this.paymentsNumReceived = 0;
        this.totalAmountReceived = Double.valueOf(0);
        this.otherFees = Double.valueOf(0);
        this.totalNetAmount = Double.valueOf(0);
        this.openingBalance = Double.valueOf(0);
        this.closingBalance = Double.valueOf(0);
        this.totalTax = Double.valueOf(0);
        this.totalServiceFee = Double.valueOf(0);
        this.accountsReceivable = Double.valueOf(0);
        this.actualReceipts = Double.valueOf(0);
        this.outstandingAccounts = Double.valueOf(0);
    }

}
