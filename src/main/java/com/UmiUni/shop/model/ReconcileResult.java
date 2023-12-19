package com.UmiUni.shop.model;

import com.UmiUni.shop.entity.PayPalPayment;
import com.UmiUni.shop.entity.ReconcileErrorLog;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class ReconcileResult {

    private PayPalPayment paypalDBPaymentRecord;

    private PaypalTransactionRecord paypalTransactionRecord;

    private ReconcileErrorLog reconcileErrorLog;
}
