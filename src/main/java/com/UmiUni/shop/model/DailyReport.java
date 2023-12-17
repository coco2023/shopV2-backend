package com.UmiUni.shop.model;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@Builder
@ToString
public class DailyReport {

    private int paymentsReceived;

    private BigDecimal totalAmountReceived;

    private BigDecimal fees;

    private BigDecimal netAmount;

    // Constructor that initializes all BigDecimal fields
    public DailyReport() {
        this.paymentsReceived = 0;
        this.totalAmountReceived = BigDecimal.ZERO; // Initialize to BigDecimal.ZERO
        this.fees = BigDecimal.ZERO; // Initialize to BigDecimal.ZERO
        this.netAmount = BigDecimal.ZERO; // Initialize to BigDecimal.ZERO
    }

}
