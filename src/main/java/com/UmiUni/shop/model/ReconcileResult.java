package com.UmiUni.shop.model;

import com.UmiUni.shop.entity.PayPalPayment;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ReconcileResult {

    private PayPalPayment payment;

    private PaypalTransactionRecord paypalTransactionRecord;
}
